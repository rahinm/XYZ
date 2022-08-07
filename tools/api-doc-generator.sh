#!/bin/sh

# Constants
WORK_DIR_NAME=_tmp
DEBUG=1

# Determine the directory where this script resides
#SCRIPT_DIR=`dirname "$0"`
SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# Tools for generating API docs
OSA_DOC_GENERATOR=redoc-cli
SWAGGER_DOC_GENERATOR=redoc-cli
RAML_DOC_GENERATOR=raml2html
WSDL_DOC_GENERATOR="java -jar ${SCRIPT_DIR}/wsdl-doc-gen.jar"
GRPC_DOC_GENERATOR="protoc --doc_out=. --doc_opt=html,"
XSD_DOC_GENERATOR="java -cp ${SCRIPT_DIR} XsltTransform ${SCRIPT_DIR}/xs3p.xsl"


usage() 
{
    echo "Usage: $0 <api-definition.zip> <api-doc.html>"
    echo "Note : The API definition file must be comprssed in zip file format (with .zip extension)"
    echo "Note : Output HTML file name must not include any path information"

    exit 1
}


# First check if the caller has supplied a name of the input zip file
# that contains the API definition (.json|.yaml|.raml|.wsdl|.proto)
if [ $# -ne 2 ]; then
    echo "Error: Only input and output file names must be provided"
    usage
fi

# Input file name is given. Extract few attributes from the input file name
INPUT_FILE_FULL_NAME=$1
INPUT_FILE_BASE_NAME=`basename "${INPUT_FILE_FULL_NAME%.*}"`
INPUT_FILE_EXTN_NAME="${INPUT_FILE_FULL_NAME##*.}"
INPUT_DIR_NAME=`dirname ${INPUT_FILE_FULL_NAME}`
OUTPUT_FILE_NAME=$2

if [ "${DEBUG}" == "1" ]; then
    echo "Script dir name: ${SCRIPT_DIR}"
    echo "Input file name: ${INPUT_FILE_FULL_NAME}"
    echo "Input file base: ${INPUT_FILE_BASE_NAME}"
    echo "Input file extn: ${INPUT_FILE_EXTN_NAME}"
    echo "Input dir name : ${INPUT_DIR_NAME}"
fi

# Input file name must have the correct extension
if [ "${INPUT_FILE_EXTN_NAME}" != "zip" ]; then
    echo "Error: Input file does not have correct filename extension"
    usage
fi

# Check if the input file exists
if [ ! -f "${INPUT_FILE_FULL_NAME}" ]; then
    echo "Error: Input file '${INPUT_FILE_FULL_NAME}' does not exist"
fi

# Move to the target directory & create a work directory if needed
echo "Moving to the target directory ${INPUT_DIR_NAME} ..."
pushd ${INPUT_DIR_NAME} &> /dev/null
echo "PWD: 'pwd'"

if [ ! -d ${WORK_DIR_NAME} ]; then
    echo "Creating working directory ${INPUT_DIR_NAME}/${WORK_DIR_NAME} ..."
    mkdir ${WORK_DIR_NAME}
else
    # Clean up the work directory to get rid of any previous remnants
    rm -rf ${WORK_DIR_NAME}/*
fi

echo "Moving to the working directory ${INPUT_DIR_NAME}/${WORK_DIR_NAME} ..."
cd ${WORK_DIR_NAME} &> /dev/null
echo "PWD: 'pwd'"

# Uncompress the input zip file into our work directory
echo "Uncompressing  ${INPUT_FILE_FULL_NAME} ..."
unzip -a ../${INPUT_FILE_BASE_NAME}
echo "Done uncompressing"

# Get a list of files of types we are interested in
FILES=`ls ./*.{yaml,json,raml,wsdl,proto,xsd}`
NUM_FILES=${#FILES[@]}
if [ "${DEBUG}" == "1" ]; then
    echo "API spec file uncompressed: " ${FILES} [Number: ${NUM_FILES}]
fi


if [ ${NUM_FILES} -ne 1 ]; then
    echo "Error: There must be exactly 1 API definition (main) file (yaml, raml, json, wsdl, proto or xsd) present"
    popd &> /dev/null
fi

# Extact some attributes for our API definition file
API_FILE_NAME=${FILES[0]}
API_FILE_BASE="${API_FILE_NAME%.*}"
API_FILE_EXTN="${API_FILE_NAME##*.}"

if [ "${DEBUG}" == "1" ]; then
    echo "Found ${NUM_FILES} file of type of inteest [${FILES}]"
    echo "API definition file: ${API_FILE_NAME}" 
    echo "API definition base: ${API_FILE_BASE}"
    echo "API definition extn: ${API_FILE_EXTN}"
fi

# Set a default value for the final exit code (note: 0 -> Success; 1 --> Failure)
SUCCESS=1

# Now Generate API documentation
case ${API_FILE_EXTN} in
    yaml)
        echo "Info: Creating documentation for REST API coded in OAS yaml"
        ${OSA_DOC_GENERATOR} bundle -o ${API_FILE_BASE}.html ${API_FILE_NAME}
        ;;

    json)
        echo "Info: Creating documentation for REST API coded in OAS JSON"
        ${OSA_DOC_GENERATOR} bundle -o ${API_FILE_BASE}.html ${API_FILE_NAME}
        ;;
    
    raml)
        echo "Info: Creating documentation for REST API coded in RAML"
        ${RAML_DOC_GENERATOR} -i ${API_FILE_NAME} -o ${API_FILE_BASE}.html 
        ;;

    wsdl)
        echo "Info: Creating documentation for SOAP/WSDL API"
        ${WSDL_DOC_GENERATOR} ${API_FILE_NAME}
        ;;
    
    proto)
        echo "Info: Creating documentation for gRPC API"
        ${GRPC_DOC_GENERATOR}${API_FILE_BASE}.html ${API_FILE_NAME}
        ;;

    xsd)
        echo "Info: Creating documentation for XML Schema"
        ${XSD_DOC_GENERATOR} ${API_FILE_NAME} ${API_FILE_BASE}.html
        ;;

esac


# Now check if we were able to create the HTML file
if [ -f ${API_FILE_BASE}.html ]; then
    echo "API documentation successfully generated"
    SUCCESS=0
    mv ${API_FILE_BASE}.html ../${OUTPUT_FILE_NAME}
fi

# do some clean up
cd ..
if [ -d ${WORK_DIR_NAME} ]; then
   rm -rf ${WORK_DIR_NAME}
fi

# Now get back to where we started from
popd &> /dev/null
echo "Exit Code: ${SUCCESS}"

exit ${SUCCESS}


