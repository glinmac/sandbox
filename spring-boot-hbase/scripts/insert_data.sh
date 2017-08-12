#!/bin/sh

hbase shell <<EOF
put 'sandbox:users', 'jdoe', 'd:fullname', 'John Doe'
put 'sandbox:users', 'jdoe', 'd:email', 'jdoe@localhost.com'
put 'sandbox:users', 'jdoe', 'd:dob', '1970-01-01'
EOF