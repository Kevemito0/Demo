// functions/index.js
const admin = require('firebase-admin');
const { setGlobalOptions } = require('firebase-functions');
const { onValueWritten, onValueUpdated } = require('firebase-functions/v2/database');
const { onDocumentUpdated } = require('firebase-functions/v2/firestore');

// 1️⃣ Ensure all your functions default to us-central1
setGlobalOptions({ region: 'us-central1' });

// 2️⃣ Init the Admin SDK
admin.initializeApp();

// 3️⃣ Sync Realtime Database → Firestore
exports.syncSensorData = onValueWritten(
  { ref: '/sensors' },
  async (event) => {
    // In v2 RTDB triggers, event.data.after is a DataSnapshot
    const afterSnap = event.data.after;
    if (!afterSnap.exists) {
      console.log('🚫 No data, skipping.');
      return null;
    }
    const data = afterSnap.toJSON();
    console.log('🔄 syncSensorData:', data);

    // Write into Firestore
    return admin.firestore()
      .collection('sensorReadings')
      .add({
        type: data.type ?? null,
        value: data.value ?? null,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
      });
  }
);

// 4️⃣ Firestore update → send FCM if gas value flips to 1
exports.sensorAlert = onDocumentUpdated(
  { document: 'sensorReadings/{docId}' },
  async (event) => {
    const before = event.data.before.data();
    const after  = event.data.after.data();
    console.log('sensorAlert before→after:', before, after);

    // Only fire when it becomes gas===1
    if (after.type === 'gas' && after.value === 1 && before.value !== 1) {
      const payload = {
        notification: {
          title: 'Gaz Kaçağı Tespit Edildi!',
          body:  'Lütfen acil müdahale edin.',
        },
        data: { alertType: 'gas' },
        topic: 'alerts',
      };
      console.log('📣 Sending notification', payload);
      return admin.messaging().send(payload);
    }

    console.log('🔕 No alert conditions met.');
    return null;
  }
);

// 5️⃣ (Optional) React to a specific RTDB flag under /alarms/highTemperature
exports.highTempAlert = onValueUpdated(
  { ref: '/alarms/highTemperature' },
  async (event) => {
    const before = event.data.before.toJSON();
    const after  = event.data.after.toJSON();
    console.log('highTempAlert before→after:', before, after);

    if (after === true && before !== true) {
      const payload = {
        notification: {
          title: 'Yüksek Sıcaklık Uyarısı!',
          body:  'Ortam sıcaklığı tehlikeli seviyeye ulaştı.',
        },
        data: { alertType: 'highTemperature' },
        topic: 'alerts',
      };
      console.log('🔥 Sending high-temp notification');
      return admin.messaging().send(payload);
    }
    return null;
  }
);
