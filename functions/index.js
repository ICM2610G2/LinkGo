/**
 * LinkGo Cloud Functions.
 * Envia una notificacion FCM a los miembros de un grupo cuando llega un
 * mensaje nuevo al chat, usando los fcmToken guardados en /users/{uid}/fcmToken.
 */
const { onValueCreated } = require("firebase-functions/database");
const { logger } = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.notifyOnNewMessage = onValueCreated(
  "/chats/{groupId}/messages/{messageId}",
  async (event) => {
    const message = event.data.val();
    const { groupId } = event.params;
    if (!message) return;

    const senderId = message.senderId || "";
    const senderName = message.senderName || "Alguien";
    const text = message.text || "";

    const db = admin.database();

    // 1. Miembros del grupo (excluyendo al emisor).
    const membersSnap = await db.ref(`/groups/${groupId}/members`).get();
    const members = membersSnap.val() || {};
    const recipientIds = Object.keys(members).filter(
      (uid) => members[uid] === true && uid !== senderId
    );
    if (recipientIds.length === 0) return;

    // 2. Tokens FCM de cada destinatario.
    const tokenSnaps = await Promise.all(
      recipientIds.map((uid) => db.ref(`/users/${uid}/fcmToken`).get())
    );
    const tokens = tokenSnaps
      .map((snap) => snap.val())
      .filter((token) => typeof token === "string" && token.length > 0);

    if (tokens.length === 0) {
      logger.info(`Sin tokens para el grupo ${groupId}`);
      return;
    }

    // 3. Envio multicast.
    const response = await admin.messaging().sendEachForMulticast({
      tokens,
      notification: {
        title: senderName,
        body: text,
      },
      data: {
        groupId: String(groupId),
        type: "chat",
      },
    });

    logger.info(
      `Notificacion enviada al grupo ${groupId}: ` +
        `${response.successCount} ok, ${response.failureCount} fallidas`
    );
  }
);
