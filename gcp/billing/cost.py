#!/usr/bin/env python
"""Misc client calls to stop instances, dataflow, ...
Can be wrapped in Cloud Function + Pub/Sub billing notifications to shutdown
instance, clusters after budget exhausted
"""
import base64
import json
import os
from googleapiclient import discovery
from oauth2client.client import GoogleCredentials

PROJECT_ID = os.getenv('GCP_PROJECT')
PROJECT_NAME = f'projects/{PROJECT_ID}'

GCE_CLIENT = None
DATAPROC_CLIENT = None
ZONES = None
REGIONS = None


def get_gce_client():
    global GCE_CLIENT
    if GCE_CLIENT is None:
        GCE_CLIENT = discovery.build(
            'compute',
            'v1',
            cache_discovery=False,
            credentials=GoogleCredentials.get_application_default()
        )
    return GCE_CLIENT


def get_dataproc_client():
    global DATAPROC_CLIENT
    if DATAPROC_CLIENT is None:
        DATAPROC_CLIENT = discovery.build(
            'dataproc',
            'v1',
            cache_discovery=False,
            credentials=GoogleCredentials.get_application_default()
        )
    return DATAPROC_CLIENT

def get_regions():
    global REGIONS
    if REGIONS is None:
        res = get_gce_client() \
            .regions() \
            .list(project=PROJECT_ID,
                  fields='items/name') \
            .execute()
        REGIONS = set([r['name'] for r in res['items']])
    return REGIONS


def get_zones():
    global ZONES
    if ZONES is None:
        res = get_gce_client() \
            .zones() \
            .list(project=PROJECT_ID, fields='items/name') \
            .execute()
        ZONES = set([zone['name'] for zone in res['items']])
    return ZONES


def decode_message(data):
    pubsub_data = base64.b64decode(data).decode('utf-8')
    return json.loads(pubsub_data)


def stop_instances(data, context):
    msg = decode_message(data)
    if msg['costAmount'] <= msg['budgetAmmount']:
        return

    __stop_instances(__list_running_instances(msg['budgetDisplayName']))


def __list_running_instances(project_id):
    running_instances = []
    for zone in get_zones():
        res = get_gce_client() \
            .instances() \
            .list(project=project_id,
                  zone=zone,
                  fields='items/name,items/status') \
            .execute()
        running_instances.extend([(i['name'], zone) for i in res.get('items', []) if i['status'] == 'RUNNING'])
    return running_instances


def __stop_instances(project_id, instances):
    for name, zone in instances:
        get_gce_client() \
            .instances() \
            .stop(project=project_id,
                  zone=zone,
                  instance=name) \
            .execute()
        print(f'Instance stopped successfully: {zone}/{name}')


def __list_dataproc_clusters(project_id):
    for region in get_regions():
        print(get_dataproc_client().projects().regions().clusters().list(projectId=project_id, region=region).execute())


if __name__ == '__main__':
    import sys

    project = sys.argv[1]
    # i = __list_running_instances(project)
    # __stop_instances(project, i)
    __list_dataproc_clusters(project)
