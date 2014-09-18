watchface-template
==================

Requires Python 2.7.x.

Usage:

- Clone the repo and give it a new name (denoted here by *&lt;new-watchface-name&gt;*)

	```
	git clone git@github.com:twotoasters/watchface-template.git <new-watchface-name>
	```

- Navigate into the newly created folder with your repo and run the project generation script with the desired input parameter values

	```
	cd <new-watchface-name>
	./generate-project.py -w <watchfaceName> -a <appName> -p <packageName>
	```

- Remove the Git history and re-add the dependent *watchface-gears* submodule

	```
	rm -rf .git
	rm -rf submodules/watchface-gears
	git init
	git submodule add git@github.com:twotoasters/watchface-gears.git submodules/watchface-gears
	```

- Start implementing your watchface in the `wear` module's `Watchface` class