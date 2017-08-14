from .default import DefaultConfig

class ProdConfig(DefaultConfig):
    DEBUG = False
    TESTING = False
    DEBUG_TOOLBAR = False