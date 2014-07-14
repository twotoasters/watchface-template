watchface-template
==================

1) Run `git clone git@github.com:twotoasters/watchface-template.git new-watchface-name`
2) Run `cd new-watchface-name`
3) Run `./generate-project.py -w <watchfaceName> -a <appName> -p <packageName>`
4) Run `rm -rf .git`
5) Run `rm -rf submodules/watchface-gears`
6) Run `git init`
7) Run `git submodule add git@github.com:twotoasters/watchface-gears.git submodules/watchface-gears`
8) Start creating the watchface