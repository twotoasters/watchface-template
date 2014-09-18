#!/usr/bin/python
from __future__ import print_function  # Gives Python 2.6+ support.
import sys, os, re, fileinput, shutil

proj_root_dir = os.path.dirname(os.path.abspath(__file__))

TEMPLATE_WATCH_NAME = 'TemplateWatchfaceName'
TEMPLATE_APP_NAME = 'TemplateAppName'
TEMPLATE_PKG_NAME = 'com.twotoasters.watchfacetemplate'


def main():
    # check inputs
    if len(sys.argv) != 7:
        sys.stderr.write('usage: generate-project.py -w <watchfaceName> -a <appName> -p <packageName>')
        sys.exit(1)

    # get order-agnostic params
    params = {}
    keyindices = [x for x in range(len(sys.argv) - 1) if x % 2 == 1]
    for i in keyindices:
        params[str(sys.argv[i])] = str(sys.argv[i+1])

    # run the generation
    generate_project(params['-w'], params['-a'], params['-p'])


def generate_project(watchfaceName, appName, packageName):
    print('\nGenerating project with attributes...\n\troot =\t', proj_root_dir, '\n\tface =\t', watchfaceName, '\n\tapp = \t', appName, '\n\tpkg =\t', packageName)
    get_user_input('\nPress Enter to continue...\n')
    replace_project_props(watchfaceName, appName, packageName)
    replace_manifests(packageName)
    replace_layout_files(packageName)
    replace_source_packages(packageName)
    print('\nProject generated!')


def replace_all(filename, searchexp, replaceExp):
    for line in fileinput.input(filename, inplace=True):
        if searchexp in line:
            line = line.replace(searchexp, replaceExp)
        sys.stdout.write(line)


def rm_tree(dirpath):
    try:
        shutil.rm_tree(dirpath)
    except:
        pass


def replace_project_props(watchfaceName, appName, packageName):
    print('Configuring project build files...')
    filename = proj_root_dir + '/gradle.properties'
    replace_all(filename, TEMPLATE_WATCH_NAME, watchfaceName)
    replace_all(filename, TEMPLATE_APP_NAME, appName)
    replace_all(filename, TEMPLATE_PKG_NAME, packageName)


def replace_manifests(packageName):
    for project_type in ['mobile', 'wear']:
        print('Configuring', project_type, 'manifest...')
        filename = proj_root_dir + '/' + project_type + '/src/main/AndroidManifest.xml'
        replace_all(filename, TEMPLATE_PKG_NAME, packageName)


def replace_layout_files(packageName):
    print('Configuring wear layouts...')
    filename = proj_root_dir + '/wear/src/main/res/layout/watchface.xml'
    replace_all(filename, TEMPLATE_PKG_NAME, packageName)


def replace_source_packages(packageName):
    print('Configuring wear sources...')
    TEMPLATE_PKG_PATH = TEMPLATE_PKG_NAME.replace('.', '/')

    # Configure package declarations and imports
    for filepath, subdirs, filenames in os.walk(proj_root_dir+'/wear/src/main/java'):
        for filename in filenames:
            file_path_and_name = os.path.join(filepath, filename)
            newfile_path_and_name = file_path_and_name.replace(TEMPLATE_PKG_PATH, packageName.replace('.', '/'))
            replace_all(file_path_and_name, TEMPLATE_PKG_NAME, packageName)

    # Configure project directory structure
    srcdir = proj_root_dir + '/wear/src/main/java/' + TEMPLATE_PKG_PATH
    destdir = srcdir.replace(TEMPLATE_PKG_PATH, packageName.replace('.', '/'))
    rm_tree(destdir)
    shutil.move(srcdir, destdir)
    rm_tree(srcdir)


def get_user_input(prompt):
    try:  # try statment gives python 2 support to input.
        return raw_input(prompt)
    except:
        return input(prompt)


if __name__ == '__main__':
    main()
