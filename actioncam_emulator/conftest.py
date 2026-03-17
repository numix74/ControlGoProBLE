collect_ignore_glob = []

# Disable broken system-level pytest-vcr plugin (urllib3 conflict)
def pytest_configure(config):
    config.pluginmanager.set_blocked("vcr")
