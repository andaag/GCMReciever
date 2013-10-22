#!/usr/bin/python
import argparse

import lib

parser = argparse.ArgumentParser(description='Send a GCM Message.')
parser.add_argument('--title', nargs='?', help='Title', required=True)
parser.add_argument('--message', nargs='?', help='Content message')
parser.add_argument('--delay_while_idle', nargs='?',
                    help='Whether or not to delay delivery until device wakes up (true/1/yes)')
parser.add_argument('--expires', nargs='?', help='Number of seconds until message expires from device')
parser.add_argument('--icon', nargs='?', help='alert or info, default info')
parser.add_argument('--iconBackground', nargs='?',
                    help="Supported : #RRGGBB #AARRGGBB 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta', 'yellow', 'lightgray', 'darkgray'")
parser.add_argument('--collapseKey', nargs='?', help='only one of these events will show up in the app')
parser.add_argument('--noNotification', help='Dont show notification', action='store_true')

##Notifications
notificationParser = parser.add_argument_group('Notifications')
notificationParser.add_argument('--notificationKey', nargs='?',
                                help='Only one of these notifications will be visible at the time (can be same as collapseKey)')
notificationParser.add_argument('--progress', nargs='?', help='0-100, 0 = indeterminate progress')
notificationParser.add_argument('--vibrate', help='Enable notification vibrate', action='store_true')
notificationParser.add_argument('--noSound', help='Disable notification sound', action='store_false')
notificationParser.add_argument('--priority', nargs='?',
                                help='-1 = low prio, 0 = default, anything above = higher. See http://developer.android.com/reference/android/app/Notification.html')

##Heartbeats
notificationParser = parser.add_argument_group('Heartbeats')
notificationParser.add_argument('--heartbeat', nargs='?',
                                help='a key to identify this unique heartbeat')
notificationParser.add_argument('--interval', nargs='?',
                                help='Interval in seconds on how often this heartbeat is expected (ommited/0 = disable heartbeat)')

#todo : if sending fails, queue and resend? (and or write to log that something FATAL happened!)

args = parser.parse_args()

delay_while_idle = False
if args.delay_while_idle is not None:
    delay_while_idle = args.delay_while_idle.lower() in ['true', '1', 'yes']

notification = None
if not args.noNotification:
    notification = lib.get_notification(notification_key=args.notificationKey, progress=args.progress,
                                        vibrate=args.vibrate, sound=args.noSound, priority=args.priority)

heartbeat = None
if args.heartbeat:
    heartbeat = lib.get_heartbeat(args.heartbeat, args.interval)

msg = lib.get_message(args.title, message=args.message, delay_while_idle=delay_while_idle, expires=args.expires,
                      icon=args.icon, icon_background=args.iconBackground, collapse_key=args.collapseKey,
                      notification=notification, heartbeat=heartbeat)
print msg
lib.send_message(msg)