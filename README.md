# Gerenciador de Tarefas para Android

Monitor de desempenho e gerenciador de processos para Android, inspirado no Gerenciador de Tarefas do Windows.
Feito com Kotlin + Jetpack Compose.

## Recursos

| Recurso | Modo limitado | Modo completo (Shizuku) |
|---|---|---|
| RAM total / usada do sistema | ✅ | ✅ |
| Frequência por núcleo da CPU | ✅ | ✅ |
| Uso real de CPU do sistema | ❌ (estimado) | ✅ |
| Lista de processos com RAM e CPU | ❌ | ✅ |
| Encerrar apps | ⚠️ só em segundo plano | ✅ (`am force-stop`) |
| Uso da GPU | ❌ | ✅ em GPUs Qualcomm (Adreno) |

> **Por que o modo limitado existe?** Desde o Android 8, apps comuns não enxergam processos de outros apps
> nem podem encerrá-los de verdade. O [Shizuku](https://github.com/RikkaApps/Shizuku) concede ao app
> permissão de nível ADB sem precisar de root.

## Limpar apps em segundo plano
O botão **Limpar apps em segundo plano** encerra os apps que não estão em uso e mostra quanta RAM foi liberada.
O efeito é temporário: o Android pode reabrir alguns apps. No modo Shizuku usa `am kill-all`; no modo limitado,
`killBackgroundProcesses` em cada app instalado.

## Como usar o Shizuku
1. Instale o app **Shizuku** (Play Store ou GitHub).
2. Inicie o serviço (Depuração sem fio nas Opções do desenvolvedor, ou root).
3. Abra este app → aba Desempenho → **Ativar Shizuku** → permitir.

## Gerar o APK pelo GitHub Actions
1. Envie o projeto para um repositório no GitHub (branch `main`).
2. Aba **Actions** → workflow **Build APK** → baixe o artefato `GerenciadorTarefas-apk`.
3. Para publicar em **Releases**: `git tag v0.1.0 && git push --tags`.

## Build local
Abra no Android Studio e rode, ou use `gradle assembleDebug`.

## Limitações conhecidas
- A leitura de processos usa `top` do toybox; o formato pode variar entre fabricantes/versões do Android.
- A GPU só é lida em aparelhos Qualcomm (`/sys/class/kgsl`). Mali/PowerVR ainda não suportados.
- Encerrar apps do sistema pode causar instabilidade. Use com cuidado.

## Licença
MIT
