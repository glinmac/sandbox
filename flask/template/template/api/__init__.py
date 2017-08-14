from flask import Blueprint

api_app = Blueprint('main',
                    __name__,
                    template_folder='templates')

from . import app