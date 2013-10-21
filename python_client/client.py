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
notificationParser.add_argument('--vibrate', nargs='?', help='only one of these events will show up in the app')
notificationParser.add_argument('--sound', nargs='?', help='only one of these events will show up in the app')
notificationParser.add_argument('--priority', nargs='?',
                                help='-1 = low prio, 0 = default, anything above = higher. See http://developer.android.com/reference/android/app/Notification.html')

args = parser.parse_args()

delay_while_idle = False
if args.delay_while_idle is not None:
    delay_while_idle = args.delay_while_idle.lower() in ['true', '1', 'yes']

notification = None
if not args.noNotification:
    notification = lib.get_notification(notificationKey=args.notificationKey, progress=args.progress,
                                        vibrate=args.vibrate, sound=args.sound, priority=args.priority)

msg = lib.get_message(args.title, message=args.message, delay_while_idle=delay_while_idle, expires=args.expires,
                      icon=args.icon, iconBackground=args.iconBackground, collapse_key=args.collapseKey,
                      notification=notification)
print msg
lib.send_message(msg)