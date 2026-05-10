# Reporting Feature Implementation

## Overview
This document describes the reporting and analytics features implemented for the BlockchainUZ backend application.

## Features Implemented

### 1. General Statistics (`/api/reports/stats`)
Returns overall blockchain statistics:
- Total blocks
- Total transactions
- Total unique addresses
- Average block time (seconds)
- Average transaction value

**Example Request:**
```bash
GET /api/reports/stats
```

**Example Response:**
```json
{
  "totalBlocks": 18946231,
  "totalTransactions": 2456789,
  "totalUniqueAddresses": 125678,
  "averageBlockTime": 12.5,
  "averageTransactionValue": 0.15
}
```

---

### 2. Transaction Volume Report (`/api/reports/volume`)
Returns transaction volume grouped by period (daily or weekly) for the last 30 days.

**Query Parameters:**
- `period` (optional): `daily` or `weekly` (default: `daily`)

**Example Request:**
```bash
GET /api/reports/volume?period=daily
```

**Example Response:**
```json
[
  {
    "date": "2026-04-29",
    "transactionCount": 1234,
    "totalVolume": 1250.75
  },
  {
    "date": "2026-04-28",
    "transactionCount": 1156,
    "totalVolume": 1180.50
  }
]
```

---

### 3. Top Active Addresses (`/api/reports/top-addresses`)
Returns the most active addresses by transaction count.

**Query Parameters:**
- `limit` (optional): Number of addresses to return (1-100, default: 10)

**Example Request:**
```bash
GET /api/reports/top-addresses?limit=10
```

**Example Response:**
```json
[
  {
    "address": "0xAddress123...",
    "transactionCount": 456
  },
  {
    "address": "0xAddress456...",
    "transactionCount": 389
  }
]
```

---

### 4. Export Transactions to CSV (`/api/reports/export/csv`)
Exports filtered transactions as CSV file with streaming response for large datasets.

**Query Parameters:**
- `startDate` (optional): Start date for filtering (ISO 8601 format, e.g., `2026-04-01`)
- `endDate` (optional): End date for filtering (ISO 8601 format, e.g., `2026-04-30`)
- `address` (optional): Filter by address (sender or receiver)

**Example Request:**
```bash
GET /api/reports/export/csv?startDate=2026-04-01&endDate=2026-04-30&address=0xAddress123
```

**Response:**
- Content-Type: `text/csv`
- Content-Disposition: `attachment; filename=transactions.csv`
- Streaming response for handling large datasets efficiently

---

### 5. Export Transactions to JSON (`/api/reports/export/json`)
Exports filtered transactions as JSON file with streaming response for large datasets.

**Query Parameters:**
- `startDate` (optional): Start date for filtering (ISO 8601 format)
- `endDate` (optional): End date for filtering (ISO 8601 format)
- `address` (optional): Filter by address (sender or receiver)

**Example Request:**
```bash
GET /api/reports/export/json?startDate=2026-04-01&endDate=2026-04-30
```

**Response:**
- Content-Type: `application/json`
- Content-Disposition: `attachment; filename=transactions.json`
- Streaming response for handling large datasets efficiently

---

## Technical Implementation

### New Files Created

#### DTOs
- `StatsDTO.java` - General statistics data transfer object
- `VolumeReportDTO.java` - Volume report data transfer object
- `TopAddressDTO.java` - Top address statistics data transfer object

#### Service Layer
- `ReportService.java` - Business logic for reporting and analytics
  - Statistics aggregation
  - Volume calculations (daily/weekly)
  - Top addresses queries
  - CSV/JSON export with streaming

#### Controller Layer
- `ReportController.java` - REST endpoints for reporting
  - `/api/reports/stats`
  - `/api/reports/volume`
  - `/api/reports/top-addresses`
  - `/api/reports/export/csv`
  - `/api/reports/export/json`

### Modified Files

#### Repositories
- `TransactionRepository.java` - Added queries:
  - `countUniqueAddresses()` - Count distinct addresses
  - `getAverageTransactionValue()` - Average transaction value
  - `findForExport()` - Filtered transaction export
  - `getDailyVolume()` - Daily volume aggregation
  - `getWeeklyVolume()` - Weekly volume aggregation
  - `findTopActiveAddresses()` - Most active addresses

- `BlockRepository.java` - Added queries:
  - `getAverageBlockTime()` - Average time between blocks

---

## Key Features

### Performance Optimizations
- **Streaming Responses**: Large dataset exports use `StreamingResponseBody` to avoid loading entire result sets into memory
- **Native Queries**: Complex aggregations use native SQL for optimal performance
- **Indexed Queries**: Leverages existing database indexes on hash, block_number, and addresses

### Data Safety
- **CSV Escaping**: Proper CSV field escaping for special characters
- **Parameter Validation**: Input validation (e.g., limit constraints)
- **Null Handling**: Safe handling of null values in aggregations

### API Design
- **RESTful Endpoints**: Clean, intuitive URL structure
- **Query Parameters**: Flexible filtering options
- **Swagger Documentation**: Full OpenAPI documentation with examples
- **Standard HTTP Headers**: Proper Content-Type and Content-Disposition headers

---

## Testing

### Manual Testing with cURL

```bash
# Get statistics
curl http://localhost:8080/api/reports/stats

# Get daily volume
curl http://localhost:8080/api/reports/volume?period=daily

# Get weekly volume
curl http://localhost:8080/api/reports/volume?period=weekly

# Get top 10 addresses
curl http://localhost:8080/api/reports/top-addresses?limit=10

# Export to CSV with date filter
curl -o transactions.csv "http://localhost:8080/api/reports/export/csv?startDate=2026-04-01&endDate=2026-04-30"

# Export to JSON with address filter
curl -o transactions.json "http://localhost:8080/api/reports/export/json?address=0xAddress123"
```

### Swagger UI
Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

---

## Notes

- All date parameters use ISO 8601 format (`YYYY-MM-DD`)
- Export endpoints support filtering by date range and/or address
- Volume reports default to last 30 days
- Transaction counts for top addresses include both sent and received transactions
- Average block time is calculated in seconds based on consecutive blocks
- Unique address count includes both sender and receiver addresses
