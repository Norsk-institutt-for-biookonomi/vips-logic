# VIPSLogic Docker Setup Guide

## 🏗️ Architecture Overview

This Docker setup provides a complete VIPSLogic development environment with:

- **VIPSLogic Application**: Java EE application running on WildFly 34.0.1.Final
- **PostgreSQL Database**: PostgreSQL 16 with PostGIS extension for geographic data
- **Database Migration**: Flyway for automated database schema management

## 🚀 Quick Start

### Prerequisites

#### 1. WildFly Configuration Setup
Copy the Docker-specific WildFly configuration to the project root:
```bash
cp wildfly_config_examples/docker/standalone.xml .
```

### Database Setup

**Direct Database Import (Recommended)**
Use direct database import for all SQL files - this approach works for both small and large datasets:

1. **Start the database container only:**
   ```bash
   docker-compose up -d vipslogicdb
   ```

2. **Wait for database to be ready:**
   ```bash
   sleep 30
   ```

3. **Import your SQL file directly:**
   ```bash
   docker exec -i vipslogicdb psql -U postgres -d vipslogic < /path/to/your/data.sql
   ```

4. **Start the application:**
   ```bash
   docker-compose up -d vipslogic
   ```

### Running the Application
### Step 1: Navigate to the VIPSLogic Directory
```bash
cd /path/to/VIPSLogic
```

### Step 2: Start the Services
```bash
docker-compose up -d
```

### Step 3: Verify the Setup
```bash
# Check if containers are running
docker ps

# Check application logs
docker logs vipslogic --tail 10

# Test API endpoints
curl http://localhost:8080/VIPSLogic/rest/organization
curl http://localhost:8080/VIPSLogic/rest/organism/list
```
