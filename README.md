# Main-Server
Główny serwer gry  
Napisany w Javie, oparty na TCP.


## Wiadomości
Naglowek wiadomosci (do autoryzacji itp) do dodania pozniej, najpierw nalwazniejsze zeby dzialalo
### Od klienta do serwera
* connected
* reconnect
* move $x $y
* ability $umiejetnosc $cel

### Od serwera do klienta
* $gracz attack $cel
* $gracz move $x $y
* id $uuid
* player $id $postac