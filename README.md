# notification_Sample
#FCM simple notification

- this app only send a notification to all users that have registered their token on the Firebase Firestore
- please don't mind the mainActivity I've only used it as a sample for offline(self notify)
- also the the notification class was supposed to be used for notification with intent as a sample.

- change the Authorization key on Utils/Constant then change the REMOTE_MSG_AUTHORIZATION key on line 25
- you can get it on the project overview on your Firebase project then users and permission then go to cloud messaging then
 turn on the legacy(cloud messaging API).
  - you can get the API key there.
