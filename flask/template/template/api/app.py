
from . import api_app

@api_app.route('/welcome')
def welcome():
    return 'Hello User!'
