#!/usr/bin/env bash
# Compila o projeto e executa os testes de aceitacao informados.
# Uso: ./run-tests.sh [us1 us1_1 ...]   (sem argumentos roda todos, na ordem do Main)
set -u
cd "$(dirname "$0")"
rm -rf out && mkdir -p out
if ! javac -encoding UTF-8 -nowarn -cp lib/easyaccept.jar -d out $(find src -name '*.java'); then
  echo "FALHA DE COMPILACAO"; exit 1
fi
rm -f bikeshare.ser
if [ "$#" -eq 0 ]; then
  java -cp out:lib/easyaccept.jar Main
else
  for t in "$@"; do
    echo "===== tests/$t.txt ====="
    java -cp out:lib/easyaccept.jar easyaccept.EasyAccept br.ufal.ic.p2.bikeshare.Facade "tests/$t.txt"
  done
fi
