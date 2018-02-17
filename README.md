# SDIS1

## Client execution

java Client <host_name> <port_number> \<oper> \<opnd>*  
>where  
>> <host_name> is the name of the host running the server;  
>> <port_number> is the server port;  
>> \<oper> is either ‘‘register’’ or ‘‘lookup’’  
>> \<opnd>* is the list of arguments  
>>> \<plate number> \<owner name>, for register;  
>>> \<plate number>, for lookup.

Example:  
> localhost 8080 register 58-AZ-85 Americo

## Server execution

java Server <port_number>  
> where  
>> <port_number> is the port number on which the server waits for requests.

Example:  
> 8080
