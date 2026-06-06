# Docker 镜像拉取失败处理

## 两件事不要搞混

| 操作 | 在哪里做 |
|------|----------|
| 配置镜像加速 JSON | **Docker Desktop 图形界面**，不是 PowerShell |
| 启动项目 | PowerShell：`docker compose up -d` |

把 JSON 粘贴进 PowerShell 会报「意外的标记 `:`」——那是 PowerShell 在把 JSON 当命令解析，不是 Docker 报错。

---

## 正确配置镜像加速（Docker Desktop）

1. 打开 **Docker Desktop**（等 Engine running）
2. 右上角 **齿轮 Settings**
3. 左侧 **Docker Engine**
4. 右侧是 JSON 编辑器，在原有内容里**只增加** `registry-mirrors` 数组，例如：

```json
{
  "builder": { "gc": { "defaultKeepStorage": "20GB", "enabled": true } },
  "experimental": false,
  "registry-mirrors": [
    "https://docker.xuanyuan.me"
  ]
}
```

> 保留你界面上已有的其他字段，只合并 `registry-mirrors`，不要整段覆盖导致别的配置丢失。

5. 点 **Apply & restart**，等 Docker 重启完成

### 更稳：阿里云专属加速（推荐国内）

1. 登录 https://cr.console.aliyun.com
2. **镜像工具** → **镜像加速器**
3. 复制你的专属地址（形如 `https://xxxx.mirror.aliyuncs.com`）
4. 填进上面 JSON 的 `registry-mirrors` 数组
5. Apply & restart

配置成功后，`docker-compose.yml` 里继续用官方名即可：

- `mysql:8.0`
- `redis:7-alpine`
- `nginx:1.25-alpine`

Docker 会自动走加速器拉取。

---

## 启动项目

```powershell
cd d:\Armando
docker compose up -d
docker compose ps
```

浏览器：http://localhost:8080

---

## 常见报错

| 报错 | 原因 | 处理 |
|------|------|------|
| `registry-1.docker.io ... EOF` | 未配加速或网络差 | 配阿里云加速器后重试 |
| `lookup docker.1ms.run: no such host` | 该镜像站域名解析失败 | 换 xuanyuan 或阿里云，勿在 compose 写死失效域名 |
| PowerShell `意外的标记 ":"` | JSON 贴进了终端 | 只在 Docker Desktop → Docker Engine 里改 |

---

## 测试加速是否生效

```powershell
docker info
```

输出里应有 `Registry Mirrors:` 和你配置的地址。

```powershell
docker pull mysql:8.0
```

能拉完再 `docker compose up -d`。
