package com.test.customfish;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @createTime: 2020/7/21
 * @author: lady_zhou
 * @Description:
 */
public class FishDrawable extends Drawable {
    private Path mPath;
    private Paint mPaint;
    private int OTHER_ALPHA = 110;
    private int BODY_ALPHA = 160;

    //转弯更自然的重心(身体的中心点)
    private PointF middlePoint;
    // 鱼的主要朝向角度
    private float fishMainAngle = 0;

    // 绘制鱼头的半径
    private float HEAD_RADIUS = 100;
    //鱼身长度
    private float BODY_LENGTH = HEAD_RADIUS * 3.2f;
    // 寻找鱼鳍起始点坐标的线长
    private float FIND_FINS_LENGTH = HEAD_RADIUS * 0.9f;
    // 鱼鳍的长度
    private float FINS_LENGTH = 1.3f * HEAD_RADIUS;
    // 大圆的半径
    private float BIG_CIRCLE_RADIUS = 0.7f * HEAD_RADIUS;
    // 中圆的半径
    private float MIDDLE_CIRCLE_RADIUS = 0.6f * BIG_CIRCLE_RADIUS;
    // 小圆半径
    private float SMALL_CIRCLE_RADIUS = 0.4f * MIDDLE_CIRCLE_RADIUS;

    // --寻找尾部中圆圆心的线长
    private final float FIND_MIDDLE_CIRCLE_LENGTH = BIG_CIRCLE_RADIUS * (0.6f + 1);
    // --寻找尾部小圆圆心的线长
    private final float FIND_SMALL_CIRCLE_LENGTH = MIDDLE_CIRCLE_RADIUS * (0.4f + 2.7f);
    // --寻找大三角形底边中心点的线长
    private final float FIND_TRIANGLE_LENGTH = MIDDLE_CIRCLE_RADIUS * 2.7f;

    public FishDrawable() {
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPath = new Path();

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);//防锯齿
        mPaint.setDither(true);//防抖
        mPaint.setARGB(OTHER_ALPHA, 244, 92, 71);

        middlePoint = new PointF(4.19f * HEAD_RADIUS, 4.19f * HEAD_RADIUS);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        float fishAngle = fishMainAngle;

        //鱼头圆心坐标
        PointF headPoint = calculatePoint(middlePoint, BODY_LENGTH / 2, fishAngle);
        canvas.drawCircle(headPoint.x, headPoint.y, HEAD_RADIUS, mPaint);

        //右鱼鳍 起点坐标
        PointF rightFinsPoint = calculatePoint(headPoint, FIND_FINS_LENGTH, fishAngle + 110);
        //右鱼鳍
        makeFins(canvas, rightFinsPoint, fishAngle, true);
        //左鱼鳍 起点坐标
        PointF leftFinsPoint = calculatePoint(headPoint, FIND_FINS_LENGTH, fishAngle - 110);
        //左鱼鳍
        makeFins(canvas, leftFinsPoint, fishAngle, false);

        // 身体底部的中心点
        PointF bodyBottomcenterPoint = calculatePoint(headPoint, BODY_LENGTH, fishAngle - 180);
        //画节肢1
        PointF middleCenterPoint = makeSegment(canvas, bodyBottomcenterPoint, BIG_CIRCLE_RADIUS, MIDDLE_CIRCLE_RADIUS,
                FIND_MIDDLE_CIRCLE_LENGTH, fishAngle, true);
        //画节肢2
        makeSegment(canvas, middleCenterPoint, MIDDLE_CIRCLE_RADIUS, SMALL_CIRCLE_RADIUS, FIND_SMALL_CIRCLE_LENGTH, fishAngle, false);

        //尾巴
        makeTriangle(canvas, middleCenterPoint, FIND_TRIANGLE_LENGTH, BIG_CIRCLE_RADIUS, fishAngle);
        makeTriangle(canvas, middleCenterPoint, FIND_TRIANGLE_LENGTH - 10, MIDDLE_CIRCLE_RADIUS, fishAngle);

        //画身体
        makeBody(canvas, headPoint, bodyBottomcenterPoint, fishAngle);
    }

    /**
     * 画鱼身体
     */
    private void makeBody(Canvas canvas, PointF headPoint, PointF bodyBottomcenterPoint, float fishAngle) {
        //求出身体的四个点
        PointF topLeftPoint = calculatePoint(headPoint, HEAD_RADIUS, fishAngle - 90);
        PointF topRightPoint = calculatePoint(headPoint, HEAD_RADIUS, fishAngle + 90);
        PointF bottomLeftPoint = calculatePoint(bodyBottomcenterPoint, BIG_CIRCLE_RADIUS, fishAngle - 90);
        PointF bottomRightPoint = calculatePoint(bodyBottomcenterPoint, BIG_CIRCLE_RADIUS, fishAngle + 90);

        //使用二阶贝塞尔曲线 得到控制点 控制鱼的胖瘦
        PointF controlLeft = calculatePoint(headPoint, BODY_LENGTH * 0.56f, fishAngle - 130);
        PointF controlRight = calculatePoint(headPoint, BODY_LENGTH * 0.56f, fishAngle + 130);

        //绘制
        mPath.reset();
        mPath.moveTo(topLeftPoint.x, topLeftPoint.y);
        mPath.lineTo(topRightPoint.x, topRightPoint.y);
        mPath.quadTo(controlRight.x, controlRight.y, bottomRightPoint.x, bottomRightPoint.y);
        mPath.lineTo(bottomLeftPoint.x, bottomLeftPoint.y);
        mPath.quadTo(controlLeft.x, controlLeft.y, topLeftPoint.x, topLeftPoint.y);

        mPaint.setAlpha(BODY_ALPHA);
        canvas.drawPath(mPath, mPaint);
    }

    /**
     * 绘制三角形
     *
     * @param canvas
     * @param startPoint
     * @param findTriangleLength 寻找三角形底边中心点的线长
     * @param triangleHalfLength 底边一半的长度
     * @param fishAngle
     */
    private void makeTriangle(Canvas canvas, PointF startPoint, float findTriangleLength, float triangleHalfLength, float fishAngle) {
        // 三角形底边的中心坐标
        PointF centerPoint = calculatePoint(startPoint, findTriangleLength, fishAngle - 180);
        // 三角形底边两点
        PointF leftPoint = calculatePoint(centerPoint, triangleHalfLength, fishAngle - 90);
        PointF rightPoint = calculatePoint(centerPoint, triangleHalfLength, fishAngle + 90);

        mPath.reset();
        mPath.moveTo(startPoint.x, startPoint.y);
        mPath.lineTo(leftPoint.x, leftPoint.y);
        mPath.lineTo(rightPoint.x, rightPoint.y);
        canvas.drawPath(mPath, mPaint);

    }

    /**
     * 画节肢
     *
     * @param canvas
     * @param bodyBottomcenterPoint 梯形上底的中心点
     * @param bigRadius             梯形大圆的半径
     * @param smallRadius           梯形小圆的半径
     * @param findSmallCircleLength 寻找梯形小圆的线长
     * @param fishAngle
     * @param hasBigCircle          节肢是否需要绘制大圆，true绘制
     * @return 计算节肢1的时候需要返回梯形小圆的圆心点，这个是绘制节肢2和三角形的起始点
     */
    private PointF makeSegment(Canvas canvas, PointF bodyBottomcenterPoint, float bigRadius, float smallRadius, float findSmallCircleLength, float fishAngle, boolean hasBigCircle) {
        // 梯形上底圆的圆心
        PointF upperCenterPoint = calculatePoint(bodyBottomcenterPoint, findSmallCircleLength,
                fishAngle - 180);

        //梯形的四个点
        PointF bottomLeftPoint = calculatePoint(bodyBottomcenterPoint, bigRadius, fishAngle - 90);
        PointF bottomRightPoint = calculatePoint(bodyBottomcenterPoint, bigRadius, fishAngle + 90);
        PointF upperLeftPoint = calculatePoint(upperCenterPoint, smallRadius, fishAngle - 90);
        PointF upperRightPoint = calculatePoint(upperCenterPoint, smallRadius, fishAngle + 90);

        //画梯形
        mPath.reset();
        mPath.moveTo(bottomLeftPoint.x, bottomLeftPoint.y);
        mPath.lineTo(bottomRightPoint.x, bottomRightPoint.y);
        mPath.lineTo(upperRightPoint.x, upperRightPoint.y);
        mPath.lineTo(upperLeftPoint.x, upperLeftPoint.y);
        canvas.drawPath(mPath, mPaint);

        if (hasBigCircle) {
            // 画大圆 --- 只在节肢1 上才绘画
            canvas.drawCircle(bodyBottomcenterPoint.x, bodyBottomcenterPoint.y, bigRadius, mPaint);
        }
        //画小圆
        canvas.drawCircle(upperCenterPoint.x, upperCenterPoint.y, smallRadius, mPaint);

        return upperCenterPoint;
    }

    /**
     * 画鱼鳍
     *
     * @param canvas
     * @param startPoint
     * @param fishAngle
     * @param isRight    绘制的是否是右鱼鳍，，true 是的
     */
    private void makeFins(Canvas canvas, PointF startPoint, float fishAngle, boolean isRight) {
        float controlAngle = 115;

        //鱼鳍终点
        PointF endPoint = calculatePoint(startPoint, FINS_LENGTH, fishAngle - 180);
        //控制点 （二阶贝塞尔曲线 有一个控制点，决定曲线的曲度）
        PointF controlPoint = calculatePoint(startPoint, FINS_LENGTH * 1.8f, isRight ? fishAngle + controlAngle : fishAngle - controlAngle);

        //绘制
        mPath.reset();
        //移动画笔到起始点
        mPath.moveTo(startPoint.x, startPoint.y);
        //绘制二阶贝塞尔曲线 （控制点和终点）
        mPath.quadTo(controlPoint.x, controlPoint.y, endPoint.x, endPoint.y);
        //绘制到画布
        canvas.drawPath(mPath, mPaint);

    }

    /**
     * 输入起点、长度、旋转角度计算终点
     *
     * @param startPoint 起点
     * @param length     长度
     * @param angle      旋转角度
     * @return 计算结果点
     */
    private PointF calculatePoint(PointF startPoint, float length, float angle) {
        //x坐标
        float deltaX = (float) (length * Math.cos(Math.toRadians(angle)));
        //y坐标
        float deltaY = (float) (length * Math.sin(Math.toRadians(angle)));

        return new PointF(startPoint.x + deltaX, startPoint.y + deltaY);

    }


    @Override
    public void setAlpha(int i) {
        // 设置Drawable的透明度，一般情况下将此alpha值设置给Paint
        mPaint.setAlpha(i);
    }

    /**
     * 设置了一个颜色过滤器，那么在绘制出来之前，被绘制内容的每一个像素都会被颜色过滤器改变
     */
    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        // 设置颜色滤镜，一般情况下将此值设置给Paint
        mPaint.setColorFilter(colorFilter);
    }

    /**
     * 这个值，可以根据setAlpha中设置的值进行调整。比如，alpha == 0时设置为 PixelFormat.TRANSPARENT。
     * 在alpha == 255时设置为PixelFormat.OPAQUE。在其他时候设置为 PixelFormat.TRANSLUCENT。
     * PixelFormat.OPAQUE:便是完全不透明，遮盖在他下面的所有内容
     * PixelFormat.TRANSPARENT:透明，完全不显示任何东西
     * PixelFormat.TRANSLUCENT:只有绘制的地方才覆盖底下的内容
     */
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    /**
     * 在View使用wrap_content的时候，设置固定宽度，默认为-1
     */
    @Override
    public int getIntrinsicWidth() {
        return (int) (8.38f * HEAD_RADIUS);
    }

    /**
     * 在View使用wrap_content的时候，设置固定高度，默认为-1
     */
    @Override
    public int getIntrinsicHeight() {
        return (int) (8.38f * HEAD_RADIUS);
    }
}
