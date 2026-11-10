# Software-Engineering-Experiment

软件工程实验

## 前后端分离的登录验证码系统

这是一个前后端分离、未使用第三方框架的图片验证码登录示例。系统只负责校验并显示结果，
不包含主页、登录状态保持和退出功能。

## 环境

- JDK 17 或更高版本
- Maven 3.8 或更高版本
- Apache Tomcat 10.1 或更高版本
- Jakarta Servlet 6.0

## 项目结构

```text
frontend/   静态登录页、样式和原生 JavaScript
backend/    Servlet 接口、验证码图片和 JSON 校验逻辑
```

前端部署为 `captcha-frontend.war`，后端部署为 `captcha-backend.war`。两个应用运行在
同一个 Tomcat、同一个端口下，因此不需要 CORS。

## 演示账号

账号直接写在 `backend/src/main/java/com/example/captcha/LoginServlet.java` 中：

| 用户名 | 密码 |
| --- | --- |
| `student` | `123456` |

## 构建和运行

1. 在项目根目录执行：

   ```text
   mvn clean package
   ```

   构建结果分别位于 `frontend/target/captcha-frontend.war` 和
   `backend/target/captcha-backend.war`。当前项目也已将两个部署包生成到根目录
   `target` 中。

2. 停止 Tomcat，删除以前部署的 `captcha-login.war` 及其解压目录。
3. 将下面两个部署包复制到 Tomcat 的 `webapps` 目录：

   ```text
   captcha-frontend.war
   captcha-backend.war
   ```

4. 启动 Tomcat。
5. 浏览器访问：

   ```text
   http://localhost:8080/captcha-frontend/
   ```

前端访问后端接口的地址配置在 `frontend/src/main/webapp/config.js`。

## 校验规则

- 程序内置 10 张验证码图片，每次随机调用其中一张。
- 验证码由 4 位字母和数字组成，不区分大小写。
- 验证码图片使用随机地址，地址中不包含验证码答案。
- 验证码保存在 `HttpSession` 中，没有单独的过期时间。
- 每次提交登录信息后，验证码立即作废，页面会自动加载新的验证码。
- 验证码正确但账号或密码错误时，返回“用户名或密码错误”。
- 只有验证码、用户名和密码全部正确时，才返回登录成功。

## 文件说明

- `frontend/src/main/webapp/index.html`：登录页面结构。
- `frontend/src/main/webapp/styles.css`：登录页面样式。
- `frontend/src/main/webapp/app.js`：调用后端接口的原生 JavaScript。
- `backend/src/main/java/com/example/captcha/CaptchaServlet.java`：返回随机内置验证码图片。
- `backend/src/main/java/com/example/captcha/LoginServlet.java`：校验验证码和固定账号。
- `backend/src/main/webapp/WEB-INF/web.xml`：配置后端会话 Cookie。

> 本项目用于本地学习和演示。密码以明文形式直接写在代码中，正式系统应使用数据库、
> 强密码哈希、HTTPS 和更完整的登录防护。
