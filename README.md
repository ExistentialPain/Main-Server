# Main-Server
Główny serwer gry  
Napisany w Javie, oparty na TCP.

Zmienne calkowite sa przesylane w tej kolejnosci jako  
`(byte)(0xff & (v >> 24))`  
`(byte)(0xff & (v >> 16))`  
`(byte)(0xff & (v >> 8))`  
`(byte)(0xff & v)`  
gdzie `v` to int do przeslania  
Jesli chodzi o floaty, najprawdopodobniej wczytanie inta i `reinterpret_cast`owanie go do floata powinno zadzialac.

## Wiadomości
Naglowek wiadomosci (do autoryzacji itp) do dodania pozniej, najpierw nalwazniejsze zeby dzialalo  
Wiadomosci terminowane sa przez znak nowej linii (\n)
### Od klienta do serwera
* connected
* reconnect
* move $x $y
* ability $umiejetnosc $cel

### Od serwera do klienta
* attack $gracz $umiejetnosc #cel
* move $gracz $x $y
* id $uuid
* player $id $postac