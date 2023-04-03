const functions = require("firebase-functions");
const admin = require("firebase-admin");

const serviceAccount = require("./serviceAccountKey.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://fyp-2023-fad2a-default-rtdb.asia-southeast1.firebasedatabase.app",
});

exports.createDailyDataForUsers = functions.pubsub
    .schedule("10 11 * * *")
    .onRun(async (context) => {
      try {
        // Get the current date and time
        const now = new Date(Date.now());
        const year = now.getFullYear();
        const month = now.getMonth() + 1;
        const day = now.getDate();
        const dateString = `${year}-${month}-${day + 1}`;

        // Get a list of UIDs from the "users" directory
        const usersRef = admin.database().ref("/users");
        const usersSnapshot = await usersRef.once("value");
        const uids = Object.keys(usersSnapshot.val());

        // Create a new dailyData object
        // for each user with the specified members
        const promises = uids.map(async (uid) => {
          const userDailyDataRef = admin
              .database()
              .ref(`/daily_data/${dateString}/${uid}`);
          const dailyData = {
            uid: uid,
            stepCount: 0,
            distanceWalked: 0,
            finishTime: "",
            taskDone: 0,
          };
          await userDailyDataRef.set(dailyData);
        });

        await Promise.all(promises);

        console.log(`Created dailyData objects for ${uids.length} users.`);
      } catch (error) {
        console.error("Error creating dailyData objects:", error);
      }
    });
