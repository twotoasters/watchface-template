#!/usr/bin/python
import sys, os, re, fileinput, shutil

projRootDir = os.path.dirname(os.path.abspath(__file__))

TEMPLATE_WATCH_NAME = 'TemplateWatchfaceName'
TEMPLATE_APP_NAME = 'TemplateAppName'
TEMPLATE_PKG_NAME = 'com.twotoasters.watchfacetemplate'

def replaceAll(filename, searchExp, replaceExp):
    for line in fileinput.input(filename, inplace = True):
        if searchExp in line:
            line = line.replace(searchExp, replaceExp)
        sys.stdout.write(line)

def rmTree(dirpath):
	try:
		shutil.rmtree(dirpath)
	except:
		pass

def replaceProjectProps(watchfaceName, appName, packageName):
	print 'Configuring project build files...'
	filename = projRootDir + '/gradle.properties'
	replaceAll(filename, TEMPLATE_WATCH_NAME, watchfaceName)
	replaceAll(filename, TEMPLATE_APP_NAME, appName)
	replaceAll(filename, TEMPLATE_PKG_NAME, packageName)

def replaceManifests(packageName):
	for projectType in ['mobile', 'wear']:
		print 'Configuring', projectType, 'manifest...'
		filename = projRootDir + '/' + projectType + '/src/main/AndroidManifest.xml'
		replaceAll(filename, TEMPLATE_PKG_NAME, packageName)

def replaceLayoutFiles(packageName):
	print 'Configuring wear layouts...'
	filename = projRootDir + '/wear/src/main/res/layout/watchface.xml'
	replaceAll(filename, TEMPLATE_PKG_NAME, packageName)

def replaceSourcePackages(packageName):
	print 'Configuring wear sources...'
	TEMPLATE_PKG_PATH = TEMPLATE_PKG_NAME.replace('.', '/')
	
	# Configure package declarations and imports
	for filepath, subdirs, filenames in os.walk(projRootDir+'/wear/src/main/java'):
	    for filename in filenames:
	    	filePathAndName = os.path.join(filepath, filename)
	    	newFilePathAndName = filePathAndName.replace(TEMPLATE_PKG_PATH, packageName.replace('.', '/'))
	    	replaceAll(filePathAndName, TEMPLATE_PKG_NAME, packageName)
	
	# Configure project directory structure
	srcDir = projRootDir + '/wear/src/main/java/' + TEMPLATE_PKG_PATH
	destDir = srcDir.replace(TEMPLATE_PKG_PATH, packageName.replace('.', '/'))
	rmTree(destDir)
	shutil.move(srcDir, destDir)
	rmTree(srcDir)

def generateProject(watchfaceName, appName, packageName):
	print '\nGenerating project with attributes...\n\troot =\t', projRootDir, '\n\tface =\t', watchfaceName, '\n\tapp = \t', appName, '\n\tpkg =\t', packageName
	raw_input("\nPress Enter to continue...\n")
	replaceProjectProps(watchfaceName, appName, packageName)
	replaceManifests(packageName)
	replaceLayoutFiles(packageName)
	replaceSourcePackages(packageName)
	print '\nProject generated!'

# check inputs
if len(sys.argv) != 7:
    sys.stderr.write('usage: generate-project.py -w <watchfaceName> -a <appName> -p <packageName>')
    sys.exit(1)

# get order-agnostic params
params = {}
keyIndices = [x for x in range(len(sys.argv) - 1) if x % 2 == 1]
for i in keyIndices:
    params[str(sys.argv[i])] = str(sys.argv[i+1])

# run the generation
generateProject(params['-w'], params['-a'], params['-p'])
