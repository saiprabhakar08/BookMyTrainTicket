# Logging System for BookMyTrainTicket Application

## Overview

This document describes the logging system implemented for the `LoginOperations.java` class in the BookMyTrainTicket application.

## Components

### 1. Logger Class (`Logger.java`)

A custom singleton logger that provides:
- **Thread-safe logging** using ReentrantLock
- **Multiple log levels**: DEBUG, INFO, WARN, ERROR
- **File and console output** for comprehensive monitoring
- **Timestamp formatting** with millisecond precision
- **Method entry/exit tracking** for debugging

### 2. Log File Location

- **File Path**: `logs/login_operations.log`
- **Auto-creation**: The logs directory is automatically created if it doesn't exist

## Log Levels

| Level | Description | Usage |
|-------|-------------|-------|
| DEBUG | Detailed debugging information | Method entry/exit, parameter values, query execution |
| INFO  | General operational information | Successful operations, user actions |
| WARN  | Warning conditions | Failed authentication, user not found |
| ERROR | Error conditions | Database errors, exceptions |

## Log Format

```
[YYYY-MM-DD HH:mm:ss.SSS] [LEVEL] [ClassName.methodName] Message
```

Example:
```
[2025-07-20 14:30:25.123] [INFO] [LoginOperations.authenticateUser] User authentication successful for: john_doe (ID: 123)
```

## Logged Operations in LoginOperations.java

### 1. Constructor
- Instance creation confirmation

### 2. authenticateUser()
- Method entry with username parameter
- Database query execution
- Authentication success/failure
- Password mismatch warnings
- User not found warnings
- Database errors

### 3. registerUser()
- Method entry with user details (excluding password)
- Username existence check
- Registration success/failure
- Database errors

### 4. userExists()
- Username existence verification
- Query execution results

### 5. updatePassword()
- Password update operations
- Success/failure tracking
- Database errors

### 6. getUserById()
- User retrieval by ID
- Success/failure tracking
- User not found warnings

### 7. getAllUsers()
- Bulk user retrieval operations
- Result count logging

## Benefits

1. **Audit Trail**: Complete record of all login-related operations
2. **Debugging**: Detailed method execution flow
3. **Security Monitoring**: Failed authentication attempts tracking
4. **Performance Monitoring**: Operation timing and success rates
5. **Error Tracking**: Comprehensive exception logging

## Usage Example

The logging is automatically integrated into all methods. Sample log entries:

```
[2025-07-20 14:30:25.123] [DEBUG] [LoginOperations.authenticateUser] Method entry - Parameters: param1=john_doe
[2025-07-20 14:30:25.125] [DEBUG] [LoginOperations.authenticateUser] Executing authentication query for user: john_doe
[2025-07-20 14:30:25.130] [INFO] [LoginOperations.authenticateUser] User authentication successful for: john_doe (ID: 123)
[2025-07-20 14:30:25.131] [DEBUG] [LoginOperations.authenticateUser] Method exit - Result: Authentication successful
```

## File Management

- Log files append new entries (no overwriting)
- Manual rotation needed for large files
- Consider implementing log rotation for production use

## Security Considerations

- Passwords are NOT logged for security
- User IDs and usernames are logged for audit purposes
- Consider data privacy regulations when implementing in production
