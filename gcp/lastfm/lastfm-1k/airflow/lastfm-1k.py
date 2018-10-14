#!/usr/bin/env python
## Example of airflow task that triggers a dataflow task

import datetime
import os

from airflow import models
from airflow.contrib.operators import dataflow_operator
from airflow.utils import trigger_rule
from airflow.operators import bash_operator

PROJECT='MY_PROJECT'

default_dag_args = {
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 0,
    #    'retry_delay': datetime.timedelta(minutes=5),
    'start_date': datetime.datetime.today() - datetime.timedelta(days=1)
}

with models.DAG(
        'lastfm-1k-ingest',
        schedule_interval=datetime.timedelta(days=1),
        default_args=default_dag_args) as dag:

    dataflow = dataflow_operator.DataFlowPythonOperator(
        task_id='ingest-users-dataflow',
        py_file='gs://{}/lastfm-dataset-1K/code/ingest-users.py'.format(PROJECT),
        job_name='ingest-users-dataflow',
        py_options=[ ],
        dataflow_default_options={
            'project': PROJECT,
            'region': 'europe-west1'
        },
        options={
        },
        poll_sleep=30)

    start = bash_operator.BashOperator(
        task_id='start',
        bash_command='echo "Start"'
    )
    end = bash_operator.BashOperator(
        task_id='end',
        bash_command='echo "End"'
    )

    start >> dataflow >> end