const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

exports.sensorAlert = functions.firestore.onDocumentWritten(
  {
    document: 'sensorReadings/{docId}',
    region: 'us-central1',
  },
  (event) => {
    const before = event.data?.before?.fields;
    const after = event.data?.after?.fields;

    const type = after?.type?.stringValue;
    const value = parseInt(after?.value?.integerValue || '0');
    const oldValue = parseInt(before?.value?.integerValue || '0');

    if (type === 'gas' && value === 1 && oldValue !== 1) {
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
      return admin.messaging().send(payload);
    }

    return null;
  }
);
