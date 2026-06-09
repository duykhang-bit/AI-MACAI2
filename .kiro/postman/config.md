# Postman API Configuration

## Account Info
- **Username:** leduykhang2022
- **Email:** vuthan185@gmail.com
- **Total Collections:** 116

## API Key
```
YOUR_POSTMAN_API_KEY
```

## Notable Collections
- **API LONG CHÂU** — `YOUR_COLLECTION_UID`
- API CARDLIST — `YOUR_COLLECTION_UID`
- AUTO CHALLENGE — `YOUR_COLLECTION_UID`
- Agent — `YOUR_COLLECTION_UID`
- BAPP — `YOUR_COLLECTION_UID`

## Usage
```python
import requests
headers = {'X-Api-Key': 'YOUR_POSTMAN_API_KEY'}

# List collections
requests.get('https://api.getpostman.com/collections', headers=headers)

# Get specific collection
requests.get('https://api.getpostman.com/collections/{collection_uid}', headers=headers)
```
