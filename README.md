# LandMC Menus

Menu sieci LandMC. Ten plugin rysuje je na backendzie i nie posiada żadnych danych.

Zbudowane na [`landmc-platform`](https://github.com/landmc-network/landmc-platform).

## Skąd się bierze zawartość menu

Z pluginu, który i tak jest jej właścicielem. Lista znajomych i lista serwerów przychodzą
z [`landmc-proxy`](https://github.com/landmc-network/landmc-proxy), historia kar
z [`landmc-punishments`](https://github.com/landmc-network/landmc-punishments). Ten plugin
dostaje gotową treść, rysuje ją, a kliknięcie odsyła z powrotem do tamtego pluginu.

Alternatywą było czytanie tych samych tabel drugi raz, za plecami ich właściciela. To znaczy
druga kopia każdego repozytorium i drugie miejsce, w którym trzeba pamiętać o vanishu.

Dzięki temu ten plugin można przeładować, przepisać albo wyrzucić bez dotykania czegokolwiek,
co trzyma stan. Nie ma tu też komend — `/znajomi`, `/kary` i `/serwery` żyją na proxy, obok
danych, i działają z każdego backendu bez instalowania czegokolwiek na każdym z nich.

## Moduły

| | |
|---|---|
| `menus-api` | format bajtowy, którym menu podróżuje. Publikowany, bo kompilują się z nim także pluginy po stronie proxy |
| `menus-paper` | plugin rysujący |

Nie ma `menus-proxy` — patrz wyżej.

## Menu

| | |
|---|---|
| **Znajomi** | kto jest online i gdzie. Kliknięcie dołącza, shift + prawy usuwa z listy |
| **Kary** | historia gracza, od najnowszej. Tylko do odczytu |
| **Serwery** | lista serwerów z liczbą graczy; niedostępne są oznaczone |

To, czy znajomy jest online i na jakim serwerze, ustala **proxy** przed wysłaniem. Backend nie
dostaje dość informacji, żeby wyliczyć to sam — i o to chodzi: vanish jest decyzją proxy,
a menu, które sprawdzałoby to samodzielnie, byłoby sposobem na znalezienie ukrytego admina.

## Bezpieczeństwo kanału

Wiadomości pluginowe jadą połączeniem gracza, więc zmodyfikowany klient potrafi na ten kanał
pisać. Projekt zakłada, że to nic nie daje:

- **treść menu** to wyłącznie tekst do narysowania. Podrobiona treść pokazuje jej autorowi menu
  pełne słów, które sam napisał;
- **kliknięcie** proxy przyjmuje tylko od `ServerConnection`. Wiadomość, którą klient wysyła
  sam, dociera do proxy jako `Player` i jest odrzucana, a ta wysłana do backendu jest tam
  ignorowana — więc jedyną drogą jest faktyczne kliknięcie w menu;
- **argument** jest sprawdzany, zanim gdziekolwiek trafi: nick musi wyglądać jak nick, a serwer
  musi być na skonfigurowanej liście;
- **akcja to komenda.** Usunięcie znajomego z menu to `/friend usun` — te same sprawdzenia, te
  same komunikaty i najprostsze możliwe stwierdzenie, co menu wolno: dokładnie to, co jego
  właściciel mógłby wpisać.

Każdy odczyt z kanału jest ograniczony. Pole długości, któremu się ufa, to pole długości, które
alokuje gigabajtową tablicę, gdy ktoś wyśle cztery bajty `0xFF`.

## Build

```bash
cd ../landmc-platform && ./gradlew publishToMavenLocal -Pversion=1.3.0
```

```bash
./gradlew build
```

## Czym różni się od oryginału

Przeniesione z `ServerUtilities` starego LandMC, gdzie menu było 1900 linijkami w dziesięciu
klasach, z hardkodowanymi slotami i stanem w polach:

- **menu rozpoznawane po `InventoryHolder`**, nie po tytule inventory. Stary framework
  porównywał tytuły — czyli dwa menu o tym samym nagłówku były tym samym menu, a nazwana tak
  samo skrzynka też;
- **głowy przez `PlayerProfile`** zamiast refleksji w prywatne pole `SkullMeta`;
- **stronicowanie napisane raz**, nie osobno w czterech plikach;
- **treść w `messages.yml`**, nie w kodzie;
- **klik anulowany dla całego zdarzenia**, a nie tylko dla slotów menu — gracz potrafi
  shift-klikiem wrzucić przedmiot do otwartego inventory.

## Czego tu jeszcze nie ma

Siedem pozostałych menu ze starego serwera — profil, sklep, rangi, ulepszenia, premium,
raporty — to w praktyce interfejs do ekonomii, sklepu i systemu raportów. Żadnego z nich
jeszcze nie ma, a menu bez nich byłoby witryną bez sklepu.
