#!/usr/bin/python
import sys
import lib

import argparse

parser = argparse.ArgumentParser(description='Process some integers.')
parser.add_argument('--title', nargs='?', help='Title', required=True)
parser.add_argument('--message', nargs='?', help='Content message')
parser.add_argument('--delay_while_idle', nargs='?', help='Whether or not to delay delivery until device wakes up (true/1/yes)')
parser.add_argument('--expires', nargs='?', help='Number of seconds until message expires from device')
parser.add_argument('--icon', nargs='?', help='alert or info, default info')
parser.add_argument('--iconBackground', nargs='?', help="Supported : #RRGGBB #AARRGGBB 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta', 'yellow', 'lightgray', 'darkgray'")
parser.add_argument('--collapseKey', nargs='?', help='only one of these events will show up in the app')


args = parser.parse_args()

delay_while_idle = False
if args.delay_while_idle != None:
	delay_while_idle = args.delay_while_idle.lower() in ['true', '1', 'yes']
msg = lib.createMsg(args.title, message=args.message, delay_while_idle=delay_while_idle, expires=args.expires, icon=args.icon, iconBackground=args.iconBackground,collapseKey=args.collapseKey)
print msg
lib.sendMsg(msg)