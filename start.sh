#!/bin/bash

# diretório onde está o server
dir="$(dirname $(realpath $0))"

# entro no diretório
cd "$dir"

# porta padrão
porta="3000"

# local do MD padrão
md="$1"

# Verifico se foi informado uma porta
if [[ -n "$2" ]];then
  porta="$1"
  md="$2"
fi

# inicio o server
./jre/bin/java Server "$porta" "$md"

