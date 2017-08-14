from .default import DefaultConfig
from .devel import DevelConfig
from .prod import ProdConfig
from .test import TestConfig

__all__ = [
    'DevelConfig',
    'ProdConfig',
    'TestConfig',
    'DefaultConfig'
]
