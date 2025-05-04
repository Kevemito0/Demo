
const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

const DEVICE_TOKEN = "e4NgV2aoT--FWE8apnr_lU:APA91bH7KyOvo6Uenu6T4HyztxwzTovyDQzfcgqPSbdzO3w3yaTAZ2huQx09RhTIeLxfap87s6SlzWkxntfzvZ4WSCliXt9UsM9bM9V4DWcbYh4RaSO3NOM";


exports.sendFireAlert = functions.database.ref("/sensor/yangin").onUpdate((change, context) => {
  const newValue = change.after.val();

  if (newValue === 1) {
    const payload = {
      notification: {
        title: "🚨 Yangın Uyarısı!",
        body: "Sensör yangın algıladı!",
        sound: "default"
      }
    };

    return admin.messaging().sendToDevice(DEVICE_TOKEN, payload)
      .then(response => {
        console.log("Bildirim gönderildi:", response);
      })
      .catch(error => {
        console.error("Bildirim gönderme hatası:", error);
      });
  }

  return null;
});

