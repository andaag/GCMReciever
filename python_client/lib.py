#!/usr/bin/python
import sys, os, time, json
#TODO : use full path for python-gcm
gcm_dir = os.path.join(os.path.dirname(sys.argv[0]), "python-gcm")
config_path = os.path.join(os.path.dirname(sys.argv[0]), "config.json")

if os.path.exists(gcm_dir):
    sys.path.insert(0, gcm_dir)
#todo : handle GCM import failure with auto download and unpack.
from gcm import GCM

config = json.loads(open(config_path, "r").read())
time_now = int(round(time.time() * 1000))
gcm = GCM(config["API_KEY"])


def createNotification(notificationKey=None, progress=None, vibrate=None, sound=None, priority=None):
    result = {}
    if notificationKey:
        result["notification-key"] = str(notificationKey)
    if progress:
        result["progress"] = int(progress)
    if vibrate:
        assert isinstance(vibrate, bool)
        result["vibrate"] = bool(vibrate)
    if sound != None:
        assert isinstance(sound, bool)
        result["sound"] = bool(sound)
    if priority != None:
        result["priority"] = int(priority)
    return result


def createMsg(title, message=None, delay_while_idle=False, expires=None, icon=None, iconBackground=None,
              collapseKey=None, notification=None, intent=None):
    result = {"title": str(title)}
    result["delay_while_idle"] = delay_while_idle
    assert delay_while_idle != None

    if message:
        result["message"] = str(message)
    if expires:
        result["expires"] = int(expires)
        result["expires"] = time_now + (result["expires"] * 1000)
    if icon:
        result["icon"] = str(icon)
        assert result["icon"] == "alert" or result["icon"] == "info"
    if iconBackground:
        result["icon-background"] = str(iconBackground)
    if collapseKey:
        result["collapse-key"] = str(collapseKey)
    if notification:
        assert isinstance(notification, dict)
        result["notification"] = notification
    if intent:
        assert isinstance(intent, dict)
        result["intent"] = intent
    return result


#msg = get_intent_message()
in_two_minutes = 60 * 2
msg = createMsg("This is a test 2!", message="This is much more detailed info",
                delay_while_idle=False, #if true waits until device is woken up to recieve message.
                expires=in_two_minutes,
                #NB : expires is cleared on start (before showing content) + on push msg recieved. Meaning if you recieve an old message and nothing else, one can use adb backup to pull it directly from the database.
                icon="alert", #alert/info for now. Default info
                iconBackground="red",
                #Supported : #RRGGBB #AARRGGBB 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta', 'yellow', 'lightgray', 'darkgray'
                collapseKey="onlyShowThisTypeOnceInList", #only one of these events will show up in the app
                notification=createNotification(
                    notificationKey="onlyShowThisPopupOnce",
                    #only one of these notifications will be shown, can be shared but don't have to be, note that the key in use will be this.hashCode()
                    progress=10, #in %, if 0 considered indeterminate progress
                    vibrate=True,
                    sound=False,
                    priority=2
                    #min = -2, -1 = low prio, 0 = default, anything above = higher. See http://developer.android.com/reference/android/app/Notification.html
                ),
                intent={
                    "type": "plain/text",
                    "data": "anders@codebox.no",
                    "packagename": "com.google.android.gm", #required
                    "classname": "com.google.android.gm.ComposeActivityGmail", #required
                    "extras": {
                        "android.intent.extra.EMAIL": "anders@codebox.no",
                        "android.intent.extra.SUBJECT": "Yeah this totally works!"
                    }
                }
)


def sendMsg(msg, verbose=False):
    if verbose:
        print "Sending:"
        print json.dumps(msg, indent=4, sort_keys=True)

    package = {}
    delay_while_idle = msg["delay_while_idle"]
    time_to_live = None

    if "expires" in msg:
        time_to_live = msg["expires"] - time_now
    del msg["delay_while_idle"]

    collapse_key = None
    if "collapse-key" in msg:
        collapse_key = msg["collapse-key"]

    package['data'] = json.dumps(msg)
    gcm.json_request(config["REGIDS"], data=package, collapse_key=collapse_key, time_to_live=time_to_live,
                     delay_while_idle=delay_while_idle)


if __name__ == "__main__":
    msg = createMsg("Simple message")
    sendMsg(msg, verbose=True)


##TODO : if payload too big we need to strip message to be max X chars. (and also possibly drop intent extras!)

