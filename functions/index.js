/**
 * LinkGo Cloud Functions.
 *
 * Notifica mensajes nuevos de chat por FCM. Soporta el modelo nuevo
 * /users/{uid}/fcmTokens/{tokenKey} y mantiene compatibilidad con el campo
 * legacy /users/{uid}/fcmToken.
 */
const { onValueCreated } = require("firebase-functions/database");
const { logger } = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

const INVALID_TOKEN_ERRORS = new Set([
  "messaging/invalid-registration-token",
  "messaging/registration-token-not-registered",
]);

exports.notifyOnNewMessage = onValueCreated(
  "/chats/{groupId}/messages/{messageId}",
  async (event) => {
    const message = event.data.val();
    const { groupId, messageId } = event.params;
    if (!message) return;

    const senderId = stringOrEmpty(message.senderId);
    const senderName = stringOrDefault(message.senderName, "Alguien");
    const text = stringOrEmpty(message.text);
    if (!senderId) {
      logger.warn(`Mensaje ${messageId} en grupo ${groupId} sin senderId`);
      return;
    }

    const db = admin.database();
    const membersSnap = await db.ref(`/groups/${groupId}/members`).get();
    const members = membersSnap.val() || {};
    const activeMemberIds = Object.keys(members).filter((uid) => members[uid] === true);
    const recipientIds = activeMemberIds.filter((uid) => uid !== senderId);

    if (recipientIds.length === 0) return;

    const [senderTokenRecords, recipientTokenGroups] = await Promise.all([
      readUserTokenRecords(db, senderId),
      Promise.all(recipientIds.map((uid) => readUserTokenRecords(db, uid))),
    ]);

    const senderTokens = new Set(senderTokenRecords.map((record) => record.token));
    const tokenByValue = new Map();
    const duplicateSenderTokenRefs = [];

    for (const records of recipientTokenGroups) {
      for (const record of records) {
        if (senderTokens.has(record.token)) {
          duplicateSenderTokenRefs.push(record);
          continue;
        }
        if (!tokenByValue.has(record.token)) {
          tokenByValue.set(record.token, record);
        }
      }
    }

    if (duplicateSenderTokenRefs.length > 0) {
      await removeTokenRecords(db, duplicateSenderTokenRefs);
      logger.warn(
        `Tokens del emisor encontrados bajo otros UID en grupo ${groupId}: ` +
          `${duplicateSenderTokenRefs.length} referencias limpiadas`
      );
    }

    const tokenRecords = [...tokenByValue.values()];
    const tokens = tokenRecords.map((record) => record.token);

    if (tokens.length === 0) {
      logger.info(`Sin tokens para el grupo ${groupId}`);
      return;
    }

    const response = await admin.messaging().sendEachForMulticast({
      tokens,
      data: {
        title: senderName,
        body: text,
        groupId: String(groupId),
        type: "chat",
        senderId,
        messageId: String(messageId),
      },
      android: {
        priority: "high",
      },
    });

    const invalidRecords = [];
    response.responses.forEach((sendResponse, index) => {
      const errorCode = sendResponse.error?.code;
      if (errorCode && INVALID_TOKEN_ERRORS.has(errorCode)) {
        invalidRecords.push(tokenRecords[index]);
      }
    });

    if (invalidRecords.length > 0) {
      await removeTokenRecords(db, invalidRecords);
      logger.warn(
        `Tokens FCM invalidos limpiados en grupo ${groupId}: ${invalidRecords.length}`
      );
    }

    logger.info(
      `Notificacion enviada al grupo ${groupId}: ` +
        `${response.successCount} ok, ${response.failureCount} fallidas`
    );
  }
);

async function readUserTokenRecords(db, uid) {
  const userSnap = await db.ref(`/users/${uid}`).get();
  if (!userSnap.exists()) return [];

  const records = [];
  const legacyToken = userSnap.child("fcmToken").val();
  if (isValidToken(legacyToken)) {
    records.push({
      uid,
      token: legacyToken,
      legacy: true,
      path: `/users/${uid}/fcmToken`,
    });
  }

  const tokensSnap = userSnap.child("fcmTokens");
  tokensSnap.forEach((tokenSnap) => {
    const rawValue = tokenSnap.val();
    const token = typeof rawValue === "string" ? rawValue : rawValue?.token;
    if (!isValidToken(token)) return;

    records.push({
      uid,
      token,
      legacy: false,
      path: `/users/${uid}/fcmTokens/${tokenSnap.key}`,
    });
  });

  return records;
}

async function removeTokenRecords(db, records) {
  const updates = {};
  for (const record of records) {
    updates[record.path] = null;
  }

  if (Object.keys(updates).length > 0) {
    await db.ref().update(updates);
  }
}

function isValidToken(token) {
  return typeof token === "string" && token.trim().length > 0;
}

function stringOrEmpty(value) {
  return typeof value === "string" ? value : "";
}

function stringOrDefault(value, fallback) {
  return typeof value === "string" && value.trim() ? value : fallback;
}
