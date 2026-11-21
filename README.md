# Estudo de Caso — Deitel Messenger e Buffer Seguro (Java)

Projeto prático desenvolvido para a disciplina de **Sistemas Operacionais** (IFSP - Campus Hortolândia), ministrada pelo Prof. Carlos Pagani.

**Integrantes:** Isabella Maria, Júlia Testa e Vivian Carazzatto.

## Estrutura do Repositório

- **`ch05/`**: Classes originais do problema Produtor/Consumidor (`UnsynchronizedBuffer`) e a versão sincronizada (`SynchronizedBuffer` e `SharedBufferTest2`) utilizando `synchronized`, `wait()` e `notifyAll()`.
- **`chat-buffer-seguro/`**: Aplicação gráfica em Java Swing (`DeitelMessengerBufferGUI`) inspirada no *Deitel Messenger*, demonstrando a troca de mensagens através de um buffer seguro (`MessageBuffer` e `SynchronizedMessageBuffer`) com log de concorrência em tempo real.

## Como Executar

### 1. Buffer Seguro (Terminal)
```bash
cd ch05
javac *.java
java SharedBufferTest2
```

### 2. Interface de Chat com Buffer Seguro
```bash
cd chat-buffer-seguro
javac *.java
java DeitelMessengerBufferGUI
```
