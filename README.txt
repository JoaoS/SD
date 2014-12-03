São 3 os ficheiros a correr o Dataserver, o TCPServer e o TCPClient2, os dados relativos aos ip´s estão atribuídos nos ficheiros .properties, os ip´s são relativamente simples de entender.

primaryIp=10.42.0.1
secundaryIp=10.42.0.90

o primaryIp  é relativo ao ip que corre o RMI, e o secundaryIp é relativamente ao ip do servidor secundário, a mesma lógica aplica-se no TCPserverconfigs.
Sendo todos os parâmetros passados pelos ficheiros não há necessidade de passar argumentos 

Compilar com javac *.java