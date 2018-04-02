#!/bin/bash

# Arguments: peer_id, version
# version: 1.0 for original, 2.0 for enhanced
java Peer $0 $1 224.0.1.1 8080 224.0.1.2 8081 224.0.1.3 8082
