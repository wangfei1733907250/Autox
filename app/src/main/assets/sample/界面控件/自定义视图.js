var win = floaty.rawWindow(
  <frame id="container" />
)
win.setPosition(100, 100)
win.setSize(600, 600)
win.setTouchable(false)
createCustomViewIn(win.container)
setInterval(() => {}, 1000)

/**
 * 向指定容器中添加一个自定义视图
 * @param viewGroup 父容器
 */
function createCustomViewIn(viewGroup) {
  var javaImports = JavaImporter(
    Packages.android.graphics.Color,
    Packages.android.graphics.Paint,
    Packages.android.util.TypedValue,
    Packages.android.view.View,
    Packages.android.view.ViewGroup,
    Packages.java.lang.Integer
  )
  with (javaImports) {
    var context = viewGroup.getContext()
    var dp1 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, context.getResources().getDisplayMetrics())
    var lp = new ViewGroup.LayoutParams(-1, -1)

    var paint = new Paint()
    // 使用 反射+包装 调用 java 中的重载函数
    paint.getClass().getMethod('setColor', Integer.TYPE).invoke(paint, Integer.valueOf(Color.CYAN))
    paint.setStyle(Paint.Style.STROKE)
    paint.setStrokeWidth(dp1 * 4)

    // 继承 View 重写 onDraw 方法
    var view = new JavaAdapter(View, {
      onDraw: function (canvas) {
        // 调用父类的 onDraw 方法
        this.super$onDraw(canvas)
        // 绘制一条斜线
        var dp600 = dp1 * 600
        canvas.drawLine(0, 0, dp600, dp600, paint)
      }
    }, context) // 传递父类构造函数的入参

    view.setLayoutParams(lp)
    ui.run(() => viewGroup.addView(view))
  }
}
