# Spacetime bubble, xyztr

Life modeled as a chronological sequence of bubbles. Some bubbles are shared with others, some are private, some are completely public. They can contain recordings, pictures, text, chat, documents, whatever and are shared with all other members of the bubble. All data is encrypted by default and can only be seen by the members of a bubble. Bubbles are saved in the Interplanetary Filesystem IPFS. IPFS hashes are stamped into the Bitcoin blockchain for a "paper trail".

Everything below in **bold** has been Implemented in concept code, ie using reasonable abstractions Which tries to show what the real code would be. Error handling is not well done, and cryptographic code may be naïve.

A bubble is composed of:

* A geographical or symbolic place. Probably latitude / longitude / altitude / accuracy (altitude and accuracy in meters), but could just as well be "The 19:35 night train from Stockholm to Copenhagen 2016-04-23, coach 8, compartment 7".
* **Time, when the bubble was created / started, and a time when it ended. But it could be possible to have bubbles that lasts forever, such as the International Space Station, or a store that is open 24/7, or a home**
* **Radius, which in this case really just means who are members of the bubble. I do not think the radius is needed, really, just the members**
* **Participants: List of friends, but could be Friends Arena, a smart watch, computer, projector, IoT gadgets**
* **Type: meeting, lunch, date, hockey match, ...**
* **Status: Created after the bubble happened, created when it happened, created before it happened (ie next board meeting), or speculative (did we have a bubble (Perhaps you met someone at a party you're interested in)?)**
* **All bubbles are encrypted by default and only readable to those who have the symmetric key with which to decrypt it, but encryption can be turned off if all members agree, or the other way around. In some cases anyone can read the bubble if they have the IPFS hash to it.**
* Media: **sound recordings** , film, photos, text ...

**When a bubble is first created by someone, a symmetric key is created for its encryption. After the bubble is encrypted with this symmetric key and saved to IPFS, the hash and the symmetric key (encrypted with the public key of each bubble member) is sent as a bubble handle to all invited members. They can then fetch the bubble from IPFS using the IPFS hash, use their private key to decrypt the symmetric key, and then use it to decrypt the bubble.**

**The hash returned from IPFS is locked into the Bitcoin block chain using https://tierion.com**

**If a bubble is changed, ie a new member is added to it, then a new version of the bubble is created and sent encrypted to IPFS as a new version, and the new hash saved to the Bitcoin block chain. HOWEVER, _IPFS currently does not let us save this under a symbolic name, but we can use the symmetric key as unique identifier, since a bubble will always be encrypted with the same key_.**

**When a user logs in, he uses a password. This password is used to re-create a symmetric key, with which we get his saved data from local storage. The saved data contains:**

* **His name**
* **His private and public key**
* **A list of all his friends (name and public key)**
* **A list of all bubbles he is part of (IPFS hash and with his public key encrypted symmetric key used to decrypt the bubble)**
* An alternative is to only save to localStorage the private key and hash IPFS location where to get name, public key, friends and bubbles. Yet another alternative is to save just the IPFS hash to user data in local store. With the password the user can then get that IPFS object and decrypt it. The IPFS hash could be the centralized identity we could save so that if a user crashes his app, all data can be retrieved from password and the IPFS hash. We COULD centrally save the IPFS hash under an email as key, since email can be verified. So we would know where to send the IPFS hash if it is forgotten. And if someone else gets to know that IPFS hash, no big deal - you need a password to get a symmetric key with which to decrypt it!

## Questions

* Can I connect to another node IPFS, not localhost, since that will not work on Android. Connect to my Lenovo laptop, punch hole in firewall for it?
* I think we need more data about a bubble on local storage, so did we do not have to load from IPFS to display bubble name, date, members, etc
* Can we make sure the encryption keys are not sent to third parties?
* How do users find eachother? OneName or Keybase?
* How do parties communicate?
* What if a user wants to leave the service, how do we help a user export all data?
* Is there a smart way to use IPFS as DNS? Check out lists of what people are using for IPFS: https://github.com/ipfs/awesome-ipfs/blob/master/README.md
* Job-related bubbles where one member is always "the employer"?
* Check did I use Symmetric keys: Use AES 256 bits and Asymmetric keys: Use RSA 2048 bits

## To do
* Register domain, xyztr.com is taken, all other xyztr Seems to be Free
* Register Trademark on "Spacetime Bubble"
* Make media files uploaded to IPFS as individual files instead of inside the bubble, OR let the bubble have a collection of IPFS hashes to media of different types

## xyztr - what does that mean?

**xyz** is, of course, the traditional cartesian (space) coordinates, while **t** is time. Everything in normal space can be pinpointed by a space coordinate in time, i.e. **xyzt**. Then, of course, **r** is the radius of a bubble. Spacetime bubble, **xyztr**. QED.
