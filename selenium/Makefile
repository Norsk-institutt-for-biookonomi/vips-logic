venv: venv/touchfile

venv/touchfile: requirements.txt
	python3 -m venv .venv
	. .venv/bin/activate; python3 -m pip install --upgrade -q pip; pip install -Ur requirements.txt
	touch .venv/touchfile

test: venv 
	@echo 'Run selenium tests'
	. .venv/bin/activate \
	&& pytest

