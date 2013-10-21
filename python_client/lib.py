#!/usr/bin/python
import sys
import os
import time
import json
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


def get_notification(notification_key=None, progress=None, vibrate=None, sound=None, priority=None):
    result = {}
    if notification_key:
        result["notification-key"] = str(notification_key)
    if progress:
        result["progress"] = int(progress)
    if vibrate:
        assert isinstance(vibrate, bool)
        result["vibrate"] = bool(vibrate)
    if sound is not None:
        assert isinstance(sound, bool)
        result["sound"] = bool(sound)
    if priority is not None:
        result["priority"] = int(priority)
    return result


def get_message(title, message=None, delay_while_idle=False, expires=None, icon=None,
                icon_background=None, collapse_key=None, notification=None, intent=None):
    result = {"title": str(title),
              "delay_while_idle": delay_while_idle}
    assert delay_while_idle is not None

    if message:
        result["message"] = str(message)
    if expires:
        #Convert to timestamp.
        result["expires"] = time_now + (int(expires) * 1000)
    if icon:
        result["icon"] = str(icon)
        assert result["icon"] == "alert" or result["icon"] == "info"
    if icon_background:
        result["icon-background"] = str(icon_background)
    if collapse_key:
        result["collapse-key"] = str(collapse_key)
    if notification:
        assert isinstance(notification, dict)
        result["notification"] = notification
    if intent:
        assert isinstance(intent, dict)
        result["intent"] = intent
    return result


def example():
    in_two_minutes = 60 * 2
    msg = get_message("This is a test 2!", message="This is much more detailed info",
                      delay_while_idle=False,
                      expires=in_two_minutes,
                      icon="alert",
                      icon_background="red",
                      collapse_key="onlyShowThisTypeOnceInList",
                      notification=get_notification(
                          notification_key="onlyShowThisPopupOnce",
                          progress=10,
                          vibrate=True,
                          sound=False,
                          priority=2
                      ),
                      intent={
                          "type": "plain/text",
                          "data": "anders@codebox.no",
                          "packagename": "com.google.android.gm",
                          "classname": "com.google.android.gm.ComposeActivityGmail",
                          "extras": {
                              "android.intent.extra.EMAIL": "anders@codebox.no",
                              "android.intent.extra.SUBJECT": "Yeah this totally works!"
                          }
                      })
    return msg


def send_message(message, verbose=False):
    if verbose:
        print "Sending:"
        print json.dumps(message, indent=4, sort_keys=True)

    package = {}
    delay_while_idle = message["delay_while_idle"]
    time_to_live = None

    if "expires" in message:
        time_to_live = (message["expires"] - time_now) / 1000
    del message["delay_while_idle"]

    collapse_key = None
    if "collapse-key" in message:
        collapse_key = message["collapse-key"]

    package['data'] = json.dumps(message)
    gcm.json_request(config["REGIDS"], data=package, collapse_key=collapse_key, time_to_live=time_to_live,
                     delay_while_idle=delay_while_idle)


if __name__ == "__main__":
    send_message(get_message("Simple message"), verbose=True)


##TODO : if payload too big we need to strip message to be max X chars. (and also possibly drop intent extras!)

