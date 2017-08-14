"""

"""
from flask import Flask
from flask_debugtoolbar import DebugToolbarExtension

from .api import api_app

import logging
import os


def create_app(name=__name__):
    app = Flask(name,
                static_folder='static',
                template_folder='/templates',
                instance_relative_config=True)

    # load configuration from environment
    if 'APP_SETTINGS' in os.environ:
        app.config.from_object(os.environ['APP_SETTINGS'])
    else:
        app.config.from_object('config.DefaultConfig')

    # load instance config
    app.config.from_pyfile('config.py')

    # Enable the DebugToolbar
    if app.config['DEBUG_TOOLBAR']:
        toolbar = DebugToolbarExtension(app)

    # Configure logging
    formatter = logging.Formatter(app.config['LOGGING_FORMAT'])
    for handler in app.logger.handlers:
        handler.setFormatter(formatter)
        handler.setLevel(logging.DEBUG)

    # registers our blueprints
    app.register_blueprint(api_app)

    return app
