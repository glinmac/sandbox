#!/usr/bin/env python3

import os
import glob
import cson

BOOSTNOTE_PATH='~/Documents/Boostnote'
EXPORT_PATH='notes'

for f in glob.glob(os.path.expanduser(os.path.join(BOOSTNOTE_PATH, 'notes', '*.cson'))):
    with open(f) as note:
        data  = cson.load(note)
        if data['type'] == 'MARKDOWN_NOTE':
            with open('notes/%s.md' % data['title'], 'w') as out:
                out.write(data['content'])