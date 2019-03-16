#!/usr/bin/env python

from google.cloud import storage
import googleapiclient.discovery

project_id = ''
bucket_name = ''
object_path = ''
location_id = 'europe-west1'
key_ring_id = 'test-key-ring-1'
crypto_key_id = 'test-key-1'

encryption_key = ''

key_ring_path = 'projects/%s/locations/%s/keyRings/%s' % (project_id, location_id, key_ring_id)
crypto_key_path = '%s/cryptoKeys/%s' % (key_ring_path, crypto_key_id)

kms_client = googleapiclient.discovery.build('cloudkms', 'v1')

request = kms_client.projects().locations().keyRings().cryptoKeys().get(name=crypto_key_path)
key = request.execute()
print(key)

# request = kms_client.projects().locations().keyRings().cryptoKeys().create(
#     parent=parent,
#     body={'purpose': 'ENCRYPT_DECRYPT'},
#     cryptoKeyId=crypto_key_id)
# response = request.execute()
# print('Created cryptokey %s' % response['name'])

request = kms_client.projects().locations().keyRings().cryptoKeys().decrypt(
    name=crypto_key_path,
    body={ 'ciphertext': encryption_key}
)
response = request.execute()
print(response)

storage_client = storage.Client()
bucket = storage_client.get_bucket(bucket_name)
blob = storage.Blog(path,
                    bucket,
                    encryption_key=response['plaintext'])
print('KEY', response['plaintext'])
blob.metadata = { 'x-goog-meta-key': key['primary']['name']}
blob.upload_from_string('this is a secret\na new line\nand some other secrets\n')
print(blob.metadata)
