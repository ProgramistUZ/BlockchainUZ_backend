# BlockchainUZ API - Postman Collection

## Importowanie kolekcji i środowisk

### 1. Importuj kolekcję
1. Otwórz Postman
2. Kliknij **Import** w lewym górnym rogu
3. Wybierz plik `BlockchainUZ_API.postman_collection.json`
4. Kolekcja zostanie zaimportowana z wszystkimi endpointami

### 2. Importuj środowiska (Environments)
1. Kliknij **Import** ponownie
2. Wybierz oba pliki:
   - `BlockchainUZ_Development.postman_environment.json`
   - `BlockchainUZ_Production.postman_environment.json`
3. Środowiska zostaną dodane do Postman

### 3. Wybierz środowisko
1. W prawym górnym rogu Postman wybierz dropdown "No Environment"
2. Wybierz **BlockchainUZ - Development** dla pracy lokalnej
3. Lub wybierz **BlockchainUZ - Production** dla produkcji

## Dostępne środowiska

### Development Environment
- **baseUrl**: `http://localhost:8080`
- Używane do testowania lokalnego
- Domyślnie skonfigurowane dla Sepolia testnet

### Production Environment
- **baseUrl**: `https://api.blockchainuz.com` (zaktualizuj po deployment)
- Używane dla produkcyjnej aplikacji
- Zaktualizuj URL po wdrożeniu na produkcję

## Konfiguracja zmiennych

Kolekcja używa następujących zmiennych:

- **baseUrl**: `http://localhost:8080` - URL aplikacji
- **blockHash**: Hash bloku do testowania (np. `0xabc123...`)
- **txHash**: Hash transakcji do testowania
- **walletAddress**: Adres portfela do testowania (domyślnie: `0x0000000000000000000000000000000000000000`)

### Jak ustawić zmienne:

1. Kliknij na kolekcję **BlockchainUZ API**
2. Przejdź do zakładki **Variables**
3. Zaktualizuj wartości w kolumnie **Current Value**
4. Kliknij **Save**

## Struktura endpointów

### 1. Blocks (Bloki)
- `GET /api/blocks` - Lista bloków z paginacją
- `GET /api/blocks/latest` - Najnowszy blok
- `GET /api/blocks/hash/{hash}` - Blok po hashu
- `GET /api/blocks/number/{number}` - Blok po numerze
- `GET /api/blocks/number/{number}/previous` - Poprzedni blok
- `GET /api/blocks/number/{number}/next` - Następny blok

### 2. Transactions (Transakcje)
- `GET /api/transactions` - Lista transakcji z paginacją
- `GET /api/transactions/{hash}` - Transakcja po hashu
- `GET /api/transactions/search` - Wyszukiwanie z filtrami

**Parametry wyszukiwania:**
- `hash` - Hash transakcji
- `blockNumber` - Numer bloku
- `status` - Status (CONFIRMED, PENDING, FAILED)
- `address` - Adres portfela (from lub to)

### 3. Wallets (Portfele)
- `GET /api/wallets/{address}` - Info o portfelu
- `GET /api/wallets/{address}/balance` - Saldo portfela
- `GET /api/wallets/{address}/transactions` - Historia transakcji

### 4. Sync (Synchronizacja)
- `GET /api/sync/status` - Status synchronizacji
- `POST /api/sync/trigger` - Ręczne uruchomienie sync (wymaga ADMIN)
- `POST /api/sync/range` - Sync zakresu bloków (wymaga ADMIN)

## Przykładowe użycie

### 1. Sprawdź status synchronizacji
```
GET http://localhost:8080/api/sync/status
```

### 2. Pobierz najnowszy blok
```
GET http://localhost:8080/api/blocks/latest
```

### 3. Pobierz listę bloków
```
GET http://localhost:8080/api/blocks?page=0&size=10&sort=number,desc
```

### 4. Pobierz blok po numerze
```
GET http://localhost:8080/api/blocks/number/1
```

### 5. Pobierz saldo portfela
```
GET http://localhost:8080/api/wallets/0x0000000000000000000000000000000000000000/balance
```

### 6. Wyszukaj transakcje dla adresu
```
GET http://localhost:8080/api/transactions/search?address=0x...&page=0&size=20
```

## Uwagi

1. **Paginacja**: Większość endpointów wspiera paginację:
   - `page` - Numer strony (zaczyna się od 0)
   - `size` - Liczba elementów na stronie
   - `sort` - Sortowanie (np. `number,desc`)

2. **Autoryzacja**: Niektóre endpointy (sync/trigger, sync/range) wymagają roli ADMIN

3. **Dane testowe**: Po uruchomieniu aplikacji należy poczekać na synchronizację pierwszych bloków

4. **Swagger UI**: Dokumentacja API dostępna pod: `http://localhost:8080/swagger-ui.html`

## Testowanie kroków

### Krok 1: Uruchom aplikację
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### Krok 2: Poczekaj na synchronizację
Sprawdź status:
```
GET /api/sync/status
```

### Krok 3: Testuj endpointy
Zacznij od podstawowych:
1. Get Sync Status
2. Get Latest Block
3. Get Blocks (Paginated)
4. Get Block by Number (użyj numeru z poprzedniego zapytania)

### Krok 4: Pobierz adresy z transakcji
Z bloków pobierz adresy portfeli i użyj ich do testowania:
- Get Wallet by Address
- Get Wallet Balance
- Get Wallet Transaction History

## Rozwiązywanie problemów

### 404 Not Found
- Sprawdź czy aplikacja działa: `http://localhost:8080/actuator/health`
- Sprawdź czy dane są zsynchronizowane: `/api/sync/status`

### 500 Internal Server Error
- Sprawdź logi aplikacji
- Upewnij się że PostgreSQL działa
- Sprawdź połączenie z blockchain node

### Brak danych
- Poczekaj na synchronizację bloków
- Uruchom ręczną synchronizację: `POST /api/sync/trigger`
- Zsynchronizuj konkretny zakres: `POST /api/sync/range?startBlock=1&endBlock=10`
