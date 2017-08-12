#!/bin/sh

hbase shell <<EOF
create_namespace 'sandbox'
create 'sandbox:users', { NAME => 'd' }
EOF