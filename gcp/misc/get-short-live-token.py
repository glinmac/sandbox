#!/usr/bin/env python
"""Fetch a short-lived credentials for a service account

https://cloud.google.com/iam/docs/creating-short-lived-service-account-credentials
"""
import warnings
import googleapiclient.discovery
import google.oauth2.credentials

warnings.simplefilter('ignore')


def fetch_short_lived_credentials(service_account, lifetime=300):
    """Fetch a short-lived credentials for the given service account

    :param service_account: service account email
    :param lifetime: duration of the short-lived credential, default to 300s
    :return:
    """

    body = {
        'delegates': [],
        'scope': [
            'https://www.googleapis.com/auth/cloud-platform'
        ],
        'lifetime': '{}s'.format(lifetime)
    }

    credentials, project_id = google.auth.default()

    service = googleapiclient.discovery.build(
        'iamcredentials',
        'v1',
        credentials=credentials
    )

    response = (
        service.projects()
            .serviceAccounts()
            .generateAccessToken(
            name='projects/-/serviceAccounts/{}'.format(service_account),
            body=body)
            .execute()
    )
    return response['accessToken']


if __name__ == '__main__':
    import argparse


    def validate_lifetime(value):
        """Validate lifetime
        """
        try:
            value = int(value)
            if 0 < value <= 3600:
                return value
            else:
                raise argparse.ArgumentTypeError('Invalid lifetime, must be an integer between 1 and'
                                                 ' 3600 (1hour)')
        except ValueError:
            raise argparse.ArgumentTypeError('Lifetime is not a valid integer')


    parser = argparse.ArgumentParser(
        description='Get a short-lived credentials for a given service account'
    )
    parser.add_argument('service_account',
                        metavar='SERVICE_ACCOUNT_EMAIL',
                        help='Service account email')
    parser.add_argument('--lifetime',
                        type=validate_lifetime,
                        default=300,
                        metavar='N',
                        help='Duration of the short-lived token in seconds (default 300s). '
                             'Must be an integer between 1 and 3600 (1h)')
    args = parser.parse_args()

    token = fetch_short_lived_credentials(args.service_account)
    print(token)
