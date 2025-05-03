const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

exports.sensorAlert = functions.firestore.onDocumentUpdated(
  {
    document: 'sensorReadings/{docId}',
    region: 'us-central1',
  },
  (event) => {
    const newData = event.data.after.data?.();

    if (!newData || !newData.type || newData.value === undefined) {
      console.log('❌ Veri eksik, işlem yapılmadı.');
      return null;
    }

    const type = newData.type;
    const value = parseInt(newData.value);

    console.log('✅ sensorAlert tetiklendi:', { type, value });

    if (type === 'gas' && value === 1) {
      const payload = {
        notification: {
          title: 'Gaz Kaçağı Tespit Edildi!',
          body: 'Lütfen acil müdahale edin.',
        },
        data: {
          alertType: 'gas',
        },
        topic: 'alerts',
      };
      console.log('📣 Bildirim gönderiliyor:', payload);
      return admin.messaging().send(payload);
    }

    return null;
  }
);
