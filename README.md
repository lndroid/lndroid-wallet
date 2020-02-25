Lndroid.Wallet - Lightning wallet for Android with open API
===========================================================

Lndroid.Wallet is a Lightning wallet based on [Lndroid.Framework](https://github.com/lndroid/lndroid-framework/). It is meant to demonstrate the capabilities of the framework, and with great luck to become a real wallet with an open API.

Here is the demo of how an Android app might connect to the wallet and access the Bitcoin Lightning Network over the IPC API. The demo app is [Lndroid.Messenger](https://github.com/lndroid/lndroid-messenger/), which allows you to exchange messages attached to lightning payments.

[![Lndroid Demo](http://img.youtube.com/vi/bF-1QxFTvHU/0.jpg)](https://www.youtube.com/watch?v=bF-1QxFTvHU "Lndroid Demo")

*Video hosted on YouTube*

# Here is what you see on the Lndroid demo video

1. We have Lndroid.Wallet set up on both phones, with some Lightning balance (meaning that they have open channels to some routing nodes).
2. Now Lndroid.Messenger app is opened for the first time after install.
3. App detects that it is not connected to any wallet, and starts an implicit activity intent to connect to any installed wallet that exposes the Lndroid.Framework IPC API. 
4. Wallet exports an activity with proper action, and so that activity is started by Android.
5. Wallet shows a confirmation dialog to the User, User confirms connection of the new App to the Wallet.
6. Wallet registers the new App, and returns a ComponentName, which allows the App to bind to the Wallet Service using an explicit intent.
7. App asks the Wallet for a permission to access Contact List. Wallet activity is started, and after User confirms, the App can read the contacts.
8. Contact list on both phones is empty. User clicks 'share contact' in the App, app makes proper API call and starts the Wallet activity.
9. User confirms sharing his contact info, and QR code is displayed.
10. On the other phone, user clicks 'Add contact' in the App, and Wallet activity is started.
11. User scans the QR code, which is a lightning payment request with routing hints, enters the contact name, and adds the Contact.
12. Now App can display the new contact.
13. User adds the contact on the other phone the same way.
14. Now User clicks on the Contact name in the App.
15. App requests permission to manage payments of this specific Contact. Wallet Activity is started, and User confirms.
16. The same permission is acquired on the other phone.
17. Now User types a message in the App, which is sent by the Wallet as a lightning payment with specified value in satoshis.
18. The App on the peer phone immediately displays the incoming message, and User sends a message in return.
19. It works! Your App can access Lightning Network on any phone with an Lndroid.Framework-based wallet.

# Roadmap

- [x] Basic wallet prototype to test some app
- [x] QR code codec to simplify adding/sharing of Contacts
- [ ] OS-level custom 'dangerous' permission for apps to access the wallet 
- [ ] Proper UI for admin tasks like Seed generation, Unlock, etc
- [ ] Initial onboarding UI and flow
- [ ] UI to manage channels, peers, nodes, on-chain tx
- [ ] At least 1 channel backup mechanism (local dir)
- [ ] Recovery from seed + channel backup
- [ ] On-chain tx support
- [ ] Unit tests
- [ ] Testnet release
- [ ] Mainnet release?

- [ ] Wallet password change support
- [ ] Wallet security settings (pin/bio/faceid/device-unlocked/etc)
- [ ] View wallet logs, with filters, w/ auto-search of payments/channels in logs
- [ ] Show history of method calls, w/ input/output messages, lnd messages, etc, per user
- [ ] API explorer, allowing to execute any API method w/ any params?
- [ ] Built-in API docs, auto-generated from framework sources using some annotations?
- [ ] Network/graph explorer: search the network graph, visualize routes, etc
- [ ] Authorization UI for every API method

# TODO

...This readme is just an intro, expand it to properly cover what Lndroid does, and how to use it...

# Dependencies

1. Android Room
2. Protobuf
3. AutoValue
4. Guava
5. Gson
6. Lndroid.Framework
7. Lndroid.Daemon
8. Lndmobile (lnd mobile SDK)
9. ZXing + ZXing-android-embedded for QR codes

# Important

The whole Lndroid project is at the very early stages of development. No guarantees are made about API stability, or the like. Do not use Lndroid code on Bitcoin mainnet, as you're very likely to lose your funds.

# License

MIT

# Author

Artur Brugeman, brugeman.artur@gmail.com