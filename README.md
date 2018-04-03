Abrir terminal na pasta src.

Compilar
- javac *.java

Executar:
1) Limpar estado anterior dos Peers: ./reset_peers.sh
2) Iniciar registo RMI: rmiregistry
3) Iniciar um Peer: ./start_peer.sh <peer_id> <version>
4) Iniciar a aplicação de testes: java TestApp <hostname>//Peer_<peer_id> <OPERATION> <opnd_1> <opnd_2>
--- onde OPERATION = {BACKUP, BACKUPENH, RESTORE, RESTOREENH, DELETE, DELETEENH, RECLAIM, STATE}