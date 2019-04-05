#!/usr/bin/bash
echo "Press [CTRL+C] to stop.."
#for i in {1..10}
#do
    echo -e "GET / HTTP/1.1\n\n" | openssl s_client -connect localhost:9020 -ign_eof
    sleep 1
#done
